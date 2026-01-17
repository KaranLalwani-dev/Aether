package com.karandev.aether.service.Impl;

import com.karandev.aether.dto.subscription.CheckoutRequest;
import com.karandev.aether.dto.subscription.CheckoutResponse;
import com.karandev.aether.dto.subscription.PortalResponse;
import com.karandev.aether.entity.Plan;
import com.karandev.aether.entity.User;
import com.karandev.aether.error.ResourceNotFoundException;
import com.karandev.aether.repository.PlanRepository;
import com.karandev.aether.repository.UserRepository;
import com.karandev.aether.security.AuthUtil;
import com.karandev.aether.service.PaymentProcessor;
import com.stripe.exception.StripeException;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class StripePaymentProcessor implements PaymentProcessor {

    final AuthUtil authUtil;
    final PlanRepository planRepository;
    final UserRepository userRepository;

    @Value("${client.url}")
    String frontendUrl;

    @Override
    public CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request) {
        Plan plan = planRepository.findById(request.planId()).orElseThrow(() ->
                new ResourceNotFoundException("Plan", request.planId().toString()));

        Long userId = authUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResourceNotFoundException("user", userId.toString()));

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
    public PortalResponse openCustomerPortal(Long userId) {
        return null;
    }

    @Override
    public void handleWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metaData) {
        log.info("type");
    }

}
