package com.karandev.aether.service.Impl;

import com.karandev.aether.dto.subscription.SubscriptionResponse;
import com.karandev.aether.entity.Plan;
import com.karandev.aether.entity.Subscription;
import com.karandev.aether.entity.User;
import com.karandev.aether.enums.SubscriptionStatus;
import com.karandev.aether.error.ResourceNotFoundException;
import com.karandev.aether.mapper.SubscriptionMapper;
import com.karandev.aether.repository.PlanRepository;
import com.karandev.aether.repository.ProjectMemberRepository;
import com.karandev.aether.repository.SubscriptionRepository;
import com.karandev.aether.repository.UserRepository;
import com.karandev.aether.security.AuthUtil;
import com.karandev.aether.service.SubscriptionService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

import static com.karandev.aether.enums.SubscriptionStatus.*;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SubscriptionServiceImpl implements SubscriptionService {

    AuthUtil authUtil;
    SubscriptionRepository subscriptionRepository;
    SubscriptionMapper subscriptionMapper;
    UserRepository userRepository;
    PlanRepository planRepository;
    ProjectMemberRepository projectMemberRepository;

    private final Integer FREE_TIER_PROJECTS_ALLOWED = 1;

    @Override
    public SubscriptionResponse getCurrentSubscription() {
        Long userId = authUtil.getCurrentUserId();
        var currentSubscription = subscriptionRepository.findByUserIdAndStatusIn(userId, Set.of(ACTIVE, PAST_DUE, TRAILING)).orElse(new Subscription());

        return subscriptionMapper.toSubscriptionResponse(currentSubscription);
    }

    // user has entered and has intended to create a subscription and this would happen after the checkout is done
    // We have received a customer checkout completed event, and we have created a basic subscriptionObject for him
    // But once the invoice.paid event comes that would call the respective service method and there we will get the subscription object that we created earlier.
    // And then we will just update the status there and mark the currentPeriodStart and currentPeriodEnd
    @Override
    public void activateSubscription(Long userId, Long planId, String subscriptionId, String customerId) {
        boolean exists = subscriptionRepository.existsByStripeSubscriptionId(subscriptionId);
        if(exists) return;

        User user = getUser(userId);
        Plan plan = getPlan(planId);

        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .stripeSubscriptionId(subscriptionId)
                .status(INCOMPLETE) // Invoice.paid event will be called, and it will be marked as active later on.
                .build();

        subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public void updateSubscription(String gatewaySubscriptionId, SubscriptionStatus status, Instant periodStart, Instant periodEnd, Boolean cancelAtPeriodEnd, Long planId) {
        Subscription subscription = getSubscription(gatewaySubscriptionId);
        boolean hasSubscriptionUpdated = false;

        if(status != null && status != subscription.getStatus()) {
            subscription.setStatus(status);
            hasSubscriptionUpdated = true;
        }

        if(periodStart != null && periodStart != subscription.getCurrentPeriodStart()) {
            subscription.setCurrentPeriodStart(periodStart);
            hasSubscriptionUpdated = true;
        }

        if(periodEnd != null && periodEnd != subscription.getCurrentPeriodEnd()) {
            subscription.setCurrentPeriodEnd(periodEnd);
            hasSubscriptionUpdated = true;
        }

        if(cancelAtPeriodEnd != null && cancelAtPeriodEnd != subscription.getCancelAtPeriodEnd()) {
            subscription.setCancelAtPeriodEnd(cancelAtPeriodEnd);
            hasSubscriptionUpdated = true;
        }

        if(planId != null && !planId.equals(subscription.getPlan().getId())) {
            Plan newPlan = getPlan(planId);
            subscription.setPlan(newPlan);
            hasSubscriptionUpdated = true;
        }

        if(hasSubscriptionUpdated) {
            log.debug("Subscription has been updated: {}", gatewaySubscriptionId);
            subscriptionRepository.save(subscription);
        }

    }

    @Override
    public void cancelSubscription(String gatewaySubscriptionId) {
        Subscription subscription = getSubscription(gatewaySubscriptionId);
        subscription.setStatus(CANCELED);
        subscriptionRepository.save(subscription);
    }

    @Override
    public void renewSubscriptionPeriod(String gatewaySubscriptionId, Instant periodStart, Instant periodEnd) {
        Subscription subscription = getSubscription(gatewaySubscriptionId);

        Instant newStart = periodStart != null ? periodStart : subscription.getCurrentPeriodEnd();
        subscription.setCurrentPeriodStart(newStart);
        subscription.setCurrentPeriodEnd(periodEnd);

        if(subscription.getStatus() == PAST_DUE || subscription.getStatus() == INCOMPLETE) {
            subscription.setStatus(ACTIVE);
        }

        subscriptionRepository.save(subscription);

    }

    @Override
    public void markSubscriptionPastDue(String gatewaySubscriptionId) {
        Subscription subscription = getSubscription(gatewaySubscriptionId);
        if(subscription.getStatus() == INCOMPLETE) {
            log.debug("Subscription is already past due, gatewaySubscriptionId: {}", gatewaySubscriptionId);
            return;
        }
        subscription.setStatus(INCOMPLETE);
        subscriptionRepository.save(subscription);

        // Notify user via email.
    }

    @Override
    public boolean canCreateNewProject() {
        Long userId = authUtil.getCurrentUserId();
        int count = projectMemberRepository.countProjectOwnedByUser(userId);
        SubscriptionResponse subscription = getCurrentSubscription();
        if(subscription.plan() == null) {
            return count < FREE_TIER_PROJECTS_ALLOWED;
        }
        return count < subscription.plan().maxProjects();
    }

    // utility methods
    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
    }

    private Plan getPlan(Long planId) {
        return planRepository.findById(planId).orElseThrow(() -> new ResourceNotFoundException("Plan", planId.toString()));
    }

    private Subscription getSubscription(String gatewaySubscriptionId) {
        return subscriptionRepository.findByStripeSubscriptionId(gatewaySubscriptionId).orElseThrow(() -> new ResourceNotFoundException("Subscription", gatewaySubscriptionId));
    }
}
