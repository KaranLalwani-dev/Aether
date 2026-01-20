package com.karandev.aether.mapper;

import com.karandev.aether.dto.subscription.PlanResponse;
import com.karandev.aether.dto.subscription.SubscriptionResponse;
import com.karandev.aether.entity.Plan;
import com.karandev.aether.entity.Subscription;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    SubscriptionResponse toSubscriptionResponse(Subscription subscription);

    PlanResponse toPlanResponse(Plan plan);
}
