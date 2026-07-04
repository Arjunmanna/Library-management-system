package com.LMS.Library.Management.System.service.impl;

import com.LMS.Library.Management.System.domain.PaymentGateway;
import com.LMS.Library.Management.System.domain.PaymentStatus;
import com.LMS.Library.Management.System.event.publisher.PaymentEventPublisher;
import com.LMS.Library.Management.System.mapper.PaymentMapper;
import com.LMS.Library.Management.System.modal.Payment;
import com.LMS.Library.Management.System.modal.Subscription;
import com.LMS.Library.Management.System.modal.User;
import com.LMS.Library.Management.System.payload.dto.PaymentDTO;
import com.LMS.Library.Management.System.payload.request.PaymentInitiateRequest;
import com.LMS.Library.Management.System.payload.request.PaymentVerifyRequest;
import com.LMS.Library.Management.System.payload.response.PaymentInitiateResponse;
import com.LMS.Library.Management.System.payload.response.PaymentLinkResponse;
import com.LMS.Library.Management.System.repository.PaymentRepository;
import com.LMS.Library.Management.System.repository.SubscriptionRepository;
import com.LMS.Library.Management.System.repository.UserRepository;
import com.LMS.Library.Management.System.service.PaymentService;
import com.LMS.Library.Management.System.service.gateway.RazorpayService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final RazorpayService razorpayService;
    private final PaymentMapper paymentMapper;
    private final PaymentEventPublisher paymentEventPublisher;

    @Override
    public PaymentInitiateResponse initiatePayment(PaymentInitiateRequest req)throws Exception {
        User user = userRepository.findById(req.getUserId()).get();

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setPaymentType(req.getPaymentType());
        payment.setGateway(req.getGateway());
        payment.setAmount(req.getAmount());
        payment.setDescription(req.getDescription());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionId("TXN_" + UUID.randomUUID());
        payment.setInitiatedAt(LocalDateTime.now());

        if(req.getSubscriptionId() != null){
            Subscription subscription = subscriptionRepository
                    .findById(req.getSubscriptionId())
                    .orElseThrow(()-> new Exception("subscription not found"));
            payment.setSubscription(subscription);
        }

        payment = paymentRepository.save(payment);
        PaymentInitiateResponse res = new PaymentInitiateResponse();

        if(req.getGateway()== PaymentGateway.RAZORPAY) {
            PaymentLinkResponse paymentLinkResponse = razorpayService.createPaymentLink(user, payment);


            res = PaymentInitiateResponse.builder()
                    .paymentId(payment.getId())
                    .gateway(payment.getGateway())
                    .checkoutUrl(paymentLinkResponse.getPayment_link_url())
                    .transactionId(paymentLinkResponse.getPayment_link_id())
                    .amount(payment.getAmount())
                    .description(payment.getDescription())
                    .success(true)
                    .message("payment initiated successfully")
                    .build();
            payment.setGatewayOrderId(paymentLinkResponse.getPayment_link_id());
        }
        payment.setStatus(PaymentStatus.PROCESSING);
        paymentRepository.save(payment);

        return res;
    }

    @Override
    public PaymentDTO verifyPayment(PaymentVerifyRequest req)throws Exception {
        JSONObject paymentDetails = razorpayService.fetchPaymentDetails(req.getRazorpayPaymentId());
        JSONObject notes = paymentDetails.getJSONObject("notes");
        Long paymentId = Long.parseLong((notes.optString("payment_id")));
        Payment payment = paymentRepository.findById(paymentId).get();
        boolean isValid = razorpayService.isValidPayment(req.getRazorpayPaymentId());

        if(PaymentGateway.RAZORPAY==payment.getGateway()){
            if(isValid){
                payment.setGatewayOrderId(req.getRazorpayPaymentId());
            }
        }
        if(isValid){
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setCompletedAt(LocalDateTime.now());
            payment = paymentRepository.save(payment);

            paymentEventPublisher.publishPaymentSuccessEvent(payment);
        }
        return paymentMapper.toDTO(payment);
    }

    @Override
    public Page<PaymentDTO> getAllPayments(Pageable pageable) {
        Page<Payment> payments = paymentRepository.findAll(pageable);
        return payments.map(paymentMapper :: toDTO);
    }
}
