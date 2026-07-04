package com.LMS.Library.Management.System.event.listener;

import com.LMS.Library.Management.System.exception.SubscriptionException;
import com.LMS.Library.Management.System.modal.Payment;
import com.LMS.Library.Management.System.repository.SubscriptionPlanRepository;
import com.LMS.Library.Management.System.service.SubscriptionService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static com.LMS.Library.Management.System.domain.PaymentType.*;

@Component
@RequiredArgsConstructor
public class PaymentEventListner {

    private final SubscriptionService subscriptionService;

    @Async
    @EventListener
    @Transactional
    public void handlePaymentSuccess(Payment payment)throws SubscriptionException {
        switch (payment.getPaymentType()){
            case FINE:
            case LOST_BOOK_PENALTY:
            case DAMAGED_BOOK_PENALTY:
                break;

            case MEMBERSHIP:
                subscriptionService.activateSubscription(payment.getSubscription().getId(),payment.getId());
        }
    }
}
