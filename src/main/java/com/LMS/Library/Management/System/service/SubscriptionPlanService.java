package com.LMS.Library.Management.System.service;

import com.LMS.Library.Management.System.modal.SubscriptionPlan;
import com.LMS.Library.Management.System.payload.dto.SubscriptionPlanDTO;

import java.util.List;

public interface SubscriptionPlanService {

    SubscriptionPlanDTO createSubscriptionPlan(SubscriptionPlanDTO planDTO) throws Exception;

    SubscriptionPlanDTO updateSubscriptionPlan(Long planId,SubscriptionPlanDTO planDTO) throws Exception;

    void deleteSubscriptionPlan(Long planId) throws Exception;

    List<SubscriptionPlanDTO> getAllSubscriptionPlan();

    SubscriptionPlan getBySubscriptionPlanCode(String subscriptionPlanCode) throws Exception;
}
