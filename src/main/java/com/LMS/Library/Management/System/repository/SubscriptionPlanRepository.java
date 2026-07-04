package com.LMS.Library.Management.System.repository;

import com.LMS.Library.Management.System.modal.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan,Long> {

    Boolean existsByPlanCode(String planCode);

    SubscriptionPlan findByPlanCode(String planCode);
}
