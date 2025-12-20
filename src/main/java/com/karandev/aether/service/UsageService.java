package com.karandev.aether.service;

import com.karandev.aether.dto.subscription.PlanLimitsResponse;
import com.karandev.aether.dto.subscription.UsageTodayResponse;

public interface UsageService {
    UsageTodayResponse getTodayUsageOfUser(Long userId);

    PlanLimitsResponse getCurrentSubscriptionLimitsOfUser(Long userId);
}
