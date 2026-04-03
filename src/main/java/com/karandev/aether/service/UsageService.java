package com.karandev.aether.service;

import com.karandev.aether.dto.subscription.PlanLimitsResponse;
import com.karandev.aether.dto.subscription.UsageTodayResponse;

public interface UsageService {
    void recordTokenUsage(Long userId, int actualTokens);
    void checkDailyTokensUsage();
}
