package com.LMS.Library.Management.System.service;

import com.LMS.Library.Management.System.exception.SubscriptionException;
import com.LMS.Library.Management.System.payload.dto.SubscriptionDTO;
import com.LMS.Library.Management.System.payload.response.PaymentInitiateResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SubscriptionService {
    PaymentInitiateResponse subscribe(SubscriptionDTO subscriptionDTO) throws Exception;

    SubscriptionDTO getUsersActiveSubscription(Long userId) throws Exception;

    SubscriptionDTO cancelSubscription(Long subscriptionId, String reason) throws SubscriptionException;

    SubscriptionDTO activateSubscription(Long subscriptionId, Long paymentId) throws SubscriptionException;

    List<SubscriptionDTO> getAllSubscriptions(Pageable pageable);

    void deactivateExpiredSubscriptions() throws Exception;
}
