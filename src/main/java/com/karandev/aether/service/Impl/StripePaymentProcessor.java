package com.karandev.aether.service.Impl;

import com.karandev.aether.dto.subscription.CheckoutRequest;
import com.karandev.aether.dto.subscription.CheckoutResponse;
import com.karandev.aether.dto.subscription.PortalResponse;
import com.karandev.aether.entity.Plan;
import com.karandev.aether.entity.User;
import com.karandev.aether.enums.SubscriptionStatus;
import com.karandev.aether.error.BadRequestException;
import com.karandev.aether.error.ResourceNotFoundException;
import com.karandev.aether.repository.PlanRepository;
import com.karandev.aether.repository.UserRepository;
import com.karandev.aether.security.AuthUtil;
import com.karandev.aether.service.PaymentProcessor;
import com.karandev.aether.service.SubscriptionService;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.model.tax.Registration;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

import static com.karandev.aether.enums.SubscriptionStatus.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class StripePaymentProcessor implements PaymentProcessor {

    final AuthUtil authUtil;
    final PlanRepository planRepository;
    final UserRepository userRepository;
    final SubscriptionService subscriptionService;

    @Value("${client.url}")
    String frontendUrl;

    @Override
    public CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request) {
        Plan plan = planRepository.findById(request.planId()).orElseThrow(() ->
                new ResourceNotFoundException("Plan", request.planId().toString()));

        Long userId = authUtil.getCurrentUserId();
        User user = getUser(userId);

        var params = SessionCreateParams.builder()
                .addLineItem(
                        SessionCreateParams.LineItem.builder().setPrice(plan.getStripePriceId()).setQuantity(1L).build())
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSubscriptionData(
                        // Added more functionality for the line items
                        // If the user subscribes today then the user will be charged after 30 days not at the end of the month or start of the month.
                        // If we want to fix the date of billing then we will define the billing anchors and keep the mode classic instead of flexible.
                        new SessionCreateParams.SubscriptionData.Builder()
                                .setBillingMode(SessionCreateParams.SubscriptionData.BillingMode.builder()
                                        .setType(SessionCreateParams.SubscriptionData.BillingMode.Type.FLEXIBLE)
                                        .build())
                                .build()
                )
                .setSuccessUrl(frontendUrl + "/success.html?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendUrl + "/cancel.html")
                .putMetadata("user_id", userId.toString())
                .putMetadata("plan_id", plan.getId().toString());
                // Stripe will pass on this metadata to our server via webhook
                // so that our server knows which user was trying to get which plan.

        try {
            String stripeCustomerId = user.getStripeCustomerId();
            if(stripeCustomerId == null || stripeCustomerId.isEmpty()) {
                params.setCustomerEmail(user.getUsername());
            } else {
                params.setCustomer(stripeCustomerId); // stripe customer Id
            }
            Session session = Session.create(params.build()); // SDK making api call to the Stripe Server
            return new CheckoutResponse(session.getUrl());
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PortalResponse openCustomerPortal() {
        Long userId = authUtil.getCurrentUserId();
        User user = getUser(userId);
        String stripeCustomerId = user.getStripeCustomerId();

        if(stripeCustomerId == null || stripeCustomerId.isEmpty()) {
            throw new BadRequestException("User does not have a Stripe Customer Id, UserId: {}" + userId);
        }

        try {
            var portalSession = com.stripe.model.billingportal.Session.create(
                    com.stripe.param.billingportal.SessionCreateParams.builder()
                            .setCustomer(stripeCustomerId)
                            .setReturnUrl(frontendUrl)
                            .build()
            );
            return new PortalResponse(portalSession.getUrl());
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metaData) {
        log.debug("Handling Stripe event: {}", type);

        switch (type) {
            case "checkout.session.completed" -> handleCheckoutSessionCompleted((Session) stripeObject, metaData); // one time on checkout completed
            case "customer.subscription.updated" -> handleCustomerSubscriptionUpdated((Subscription) stripeObject); // when user cancels, upgrades or any updates
            case "customer.subscription.deleted" -> handleCustomerSubscriptionDeleted((Subscription) stripeObject); // when subscription ends, revoke the access
            case "invoice.paid" -> handleInvoicePaid((Invoice) stripeObject); // when invoice is paid
            case "invoice.payment_failed" -> handleInvoicePaymentFailed((Invoice) stripeObject); // when invoice is not paid mark as past due
            default -> log.debug("Ignoring the event {}: ", type);
        }
    }

    private void handleCheckoutSessionCompleted(Session session, Map<String, String> metaData) {

        if(session == null) {
            log.error("Session Object was null");
            return;
        }
        Long userId = Long.parseLong(metaData.get("user_id"));
        Long planId = Long.parseLong(metaData.get("plan_id"));

        String subscriptionId = session.getSubscription();
        String customerId = session.getCustomer();

        User user = getUser(userId);
        if(user.getStripeCustomerId() != null) {
            user.setStripeCustomerId(customerId);
            userRepository.save(user);
        }

        subscriptionService.activateSubscription(userId, planId, subscriptionId, customerId);
    }

    private void handleCustomerSubscriptionUpdated(Subscription subscription) {
        if(subscription == null) {
            log.error("Subscription object was null");
            return;
        }

        SubscriptionStatus status = mapStripeStatusToEnum(subscription.getStatus());
        if(status == null) {
            log.warn("Unknown status {} for subscription {}", subscription.getStatus(), subscription.getId());
            return;
        }

        SubscriptionItem item = subscription.getItems().getData().get(0);
        Instant periodStart = toInstant(item.getCurrentPeriodStart());
        Instant periodEnd = toInstant(item.getCurrentPeriodEnd());

        Long planId = resolvePlanId(item.getPrice());

        subscriptionService.updateSubscription(
                subscription.getId(), status, periodStart, periodEnd,
                subscription.getCancelAtPeriodEnd(), planId
        );

    }

    private void handleCustomerSubscriptionDeleted(Subscription subscription) {
        if(subscription == null) {
            log.error("Subscription object was null in handleCustomerSubscriptionDeleted");
        }

        subscriptionService.cancelSubscription(subscription.getId());
    }

    private void handleInvoicePaid(Invoice invoice) {
        String subId = extractSubscriptionId(invoice);
        if(subId == null) return;

        Subscription subscription = null;
        try {
            subscription = Subscription.retrieve(subId); // sdk calling the stripe server
            var item = subscription.getItems().getData().get(0);

            Instant periodStart = toInstant(item.getCurrentPeriodStart());
            Instant periodEnd = toInstant(item.getCurrentPeriodEnd());

            subscriptionService.renewSubscriptionPeriod(subId, periodStart, periodEnd);

        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

    }

    private void handleInvoicePaymentFailed(Invoice invoice) {
        String subId = extractSubscriptionId(invoice);
        if(subId == null) return;

        subscriptionService.markSubscriptionPastDue(subId);
    }

    // Utility methods
    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new ResourceNotFoundException("user", userId.toString()));
    }

    private SubscriptionStatus mapStripeStatusToEnum(String status) {
        return switch(status) {
            case "active" -> ACTIVE;
            case "trialing" -> TRAILING;
            case "past_due", "unpaid", "paused", "incomplete_expired" ->  PAST_DUE;
            case "canceled" -> CANCELED;
            case "incomplete" -> INCOMPLETE;
            default -> {
                log.warn("Unmapped Stripe status: {}", status);
                yield null;
            }
        };
    }

    private Instant toInstant(Long epoch) {
        return epoch != null ? Instant.ofEpochSecond(epoch) : null;
    }

    private Long resolvePlanId(Price price) {
        if(price == null || price.getId() == null) {
            return null;
        }

        return planRepository.findByStripePriceId(price.getId()).map(Plan::getId).orElse(null);
    }

    private String extractSubscriptionId(Invoice invoice) {
        var parent = invoice.getParent();
        if(parent == null) return null;

        var subDetails = parent.getSubscriptionDetails();
        if(subDetails == null) return null;

        return subDetails.getSubscription();
    }

}
