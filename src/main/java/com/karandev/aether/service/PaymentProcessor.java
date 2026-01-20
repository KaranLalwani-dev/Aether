package com.karandev.aether.service;

import com.karandev.aether.dto.subscription.CheckoutRequest;
import com.karandev.aether.dto.subscription.CheckoutResponse;
import com.karandev.aether.dto.subscription.PortalResponse;
import com.stripe.model.StripeObject;

import java.util.Map;

public interface PaymentProcessor {

    CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request);

    PortalResponse openCustomerPortal();

    void handleWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metaData);
}
