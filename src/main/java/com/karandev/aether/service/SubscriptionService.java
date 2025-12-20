package com.karandev.aether.service;

import com.karandev.aether.dto.subscription.CheckoutRequest;
import com.karandev.aether.dto.subscription.CheckoutResponse;
import com.karandev.aether.dto.subscription.PortalResponse;
import com.karandev.aether.dto.subscription.SubscriptionResponse;
import org.jspecify.annotations.Nullable;

public interface SubscriptionService {

    SubscriptionResponse getCurrentSubscription();

    CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request, Long userId);

    PortalResponse openCustomerPortal(Long userId);
}
