package com.karandev.aether.service.Impl;

import com.karandev.aether.dto.subscription.PlanLimitsResponse;
import com.karandev.aether.dto.subscription.UsageTodayResponse;
import com.karandev.aether.service.UsageService;
import org.springframework.stereotype.Service;

@Service
public class UsageServiceImpl implements UsageService {
    @Override
    public UsageTodayResponse getTodayUsageOfUser(Long userId) {
        return null;
    }

    @Override
    public PlanLimitsResponse getCurrentSubscriptionLimitsOfUser(Long userId) {
        return null;
    }
}
