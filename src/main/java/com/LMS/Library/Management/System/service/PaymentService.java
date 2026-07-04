package com.LMS.Library.Management.System.service;

import com.LMS.Library.Management.System.payload.dto.PaymentDTO;
import com.LMS.Library.Management.System.payload.request.PaymentInitiateRequest;
import com.LMS.Library.Management.System.payload.request.PaymentVerifyRequest;
import com.LMS.Library.Management.System.payload.response.PaymentInitiateResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    PaymentInitiateResponse initiatePayment(PaymentInitiateRequest req)throws Exception;

    PaymentDTO verifyPayment(PaymentVerifyRequest req)throws Exception;

    Page<PaymentDTO> getAllPayments(Pageable pageable);
}
