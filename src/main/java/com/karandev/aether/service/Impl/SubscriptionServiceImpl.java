package com.karandev.aether.service.Impl;

import com.karandev.aether.dto.subscription.CheckoutRequest;
import com.karandev.aether.dto.subscription.CheckoutResponse;
import com.karandev.aether.dto.subscription.PortalResponse;
import com.karandev.aether.dto.subscription.SubscriptionResponse;
import com.karandev.aether.service.SubscriptionService;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {
    @Override
    public SubscriptionResponse getCurrentSubscription() {
        return null;
    }

}
