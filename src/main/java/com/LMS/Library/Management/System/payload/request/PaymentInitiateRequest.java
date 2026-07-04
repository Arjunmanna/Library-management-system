package com.LMS.Library.Management.System.payload.request;

import com.LMS.Library.Management.System.domain.PaymentGateway;
import com.LMS.Library.Management.System.domain.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentInitiateRequest {

    @NotNull(message = "user ID is mandatory")
    private Long userId;

    private Long bookLoanId;

    @NotNull(message = "payment type is mandatory")
    private PaymentType paymentType;

    @NotNull(message = "payment gateway is mandatory")
    private PaymentGateway gateway;

    @NotNull(message = "amount is mandatory")
    @Positive(message = "amount must be positive")
    private Long amount;

    @Size(max = 500,message = "description must not exceed 500 characters")
    private String description;

    private Long fineId;
    private Long subscriptionId;

    @Size(max = 500,message = "success URL must not exceed 500 characters")
    private String successUrl;

    @Size(max = 500,message = "cancel URL must not exceed 500 characters")
    private String cancelUrl;
}
