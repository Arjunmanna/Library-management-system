package com.LMS.Library.Management.System.service.impl;

import com.LMS.Library.Management.System.domain.PaymentGateway;
import com.LMS.Library.Management.System.domain.PaymentType;
import com.LMS.Library.Management.System.exception.SubscriptionException;
import com.LMS.Library.Management.System.mapper.SubscriptionMapper;
import com.LMS.Library.Management.System.modal.Subscription;
import com.LMS.Library.Management.System.modal.SubscriptionPlan;
import com.LMS.Library.Management.System.modal.User;
import com.LMS.Library.Management.System.payload.dto.SubscriptionDTO;
import com.LMS.Library.Management.System.payload.request.PaymentInitiateRequest;
import com.LMS.Library.Management.System.payload.response.PaymentInitiateResponse;
import com.LMS.Library.Management.System.repository.SubscriptionPlanRepository;
import com.LMS.Library.Management.System.repository.SubscriptionRepository;
import com.LMS.Library.Management.System.service.PaymentService;
import com.LMS.Library.Management.System.service.SubscriptionService;
import com.LMS.Library.Management.System.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final UserService userService;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PaymentService paymentService;

    @Override
    public PaymentInitiateResponse subscribe(SubscriptionDTO subscriptionDTO) throws Exception {

        User user = userService.getCurrentUser();

        SubscriptionPlan plan = subscriptionPlanRepository
                .findById(subscriptionDTO.getPlanId())
                .orElseThrow(()-> new Exception("plan not found"));

        Subscription subscription = subscriptionMapper.toEntity(subscriptionDTO,plan,user);
        subscription.initializeFromPlan();
        subscription.setIsActive(false);
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        PaymentInitiateRequest paymentInitiateRequest = PaymentInitiateRequest
                .builder()
                .userId(user.getId())
                .subscriptionId(subscription.getId())
                .paymentType(PaymentType.MEMBERSHIP)
                .gateway(PaymentGateway.RAZORPAY)
                .amount(subscription.getPrice())
                .description("Library Subscription - " + plan.getName())
                .build();

        return paymentService.initiatePayment(paymentInitiateRequest);
    }

    @Override
    public SubscriptionDTO getUsersActiveSubscription(Long userId) throws Exception {

        User user = userService.getCurrentUser();

        Subscription subscription = subscriptionRepository
                .findActiveSubscriptionByUserId(user.getId(), LocalDate.now())
                .orElseThrow(()-> new SubscriptionException("no active subscription found"));
        return subscriptionMapper.toDTO(subscription);
    }

    @Override
    public SubscriptionDTO cancelSubscription(Long subscriptionId, String reason) throws SubscriptionException {

        Subscription subscription = subscriptionRepository
                .findById(subscriptionId)
                .orElseThrow(()-> new SubscriptionException("subscription not found with ID: "+subscriptionId));

        if(!subscription.getIsActive()){
            throw new SubscriptionException("subscription is already inactive");
        }

        subscription.setIsActive(false);
        subscription.setCancelledAt(LocalDateTime.now());
        subscription.setCancellationReason(reason != null ? reason : "Cancelled By User");

        subscription = subscriptionRepository.save(subscription);
        return subscriptionMapper.toDTO(subscription);
    }

    @Override
    public SubscriptionDTO activateSubscription(Long subscriptionId, Long paymentId) throws SubscriptionException {

        Subscription subscription = subscriptionRepository
                .findById(subscriptionId)
                .orElseThrow(()-> new SubscriptionException("subscription not found with ID: "+subscriptionId));

        subscription.setIsActive(true);
        subscription = subscriptionRepository.save(subscription);

        return subscriptionMapper.toDTO(subscription);
    }

    @Override
    public List<SubscriptionDTO> getAllSubscriptions(Pageable pageable) {
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        return subscriptionMapper.toDTOList(subscriptions);
    }

    @Override
    public void deactivateExpiredSubscriptions() throws Exception {

        List<Subscription> expiredSubscriptions = subscriptionRepository
                .findExpiredActiveSubscriptions(LocalDate.now());

        for(Subscription subscription : expiredSubscriptions){
            subscription.setIsActive(false);
            subscriptionRepository.save(subscription);
        }
    }
}
