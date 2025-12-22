package com.karandev.aether.service.Impl;

import com.karandev.aether.dto.subscription.PlanResponse;
import com.karandev.aether.service.PlanService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanServiceImpl implements PlanService {
    @Override
    public List<PlanResponse> getAllActivePlans() {
        return List.of();
    }
}
