package com.LMS.Library.Management.System.payload.dto;

import com.LMS.Library.Management.System.domain.PaymentGateway;
import com.LMS.Library.Management.System.domain.PaymentStatus;
import com.LMS.Library.Management.System.domain.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {

    private Long id;

    @NotNull(message = "user ID is mandatory")
    private Long userId;

    private String userName;
    private String userEmail;
    private Long bookLoanId;
    private Long subscriptionId;

    @NotNull(message = "payment type is mandatory")
    private PaymentType paymentType;

    private PaymentStatus status;

    @NotNull(message = "payment gateway is mandatory")
    private PaymentGateway gateway;

    @NotNull(message = "amount is mandatory")
    @Positive(message = "amount must be positive")
    private Long amount;

    private String transactionId;
    private String gatewayPaymentId;
    private String gatewayOrderId;
    private String gatewaySignature;

    @Size(max = 500,message = "description must not exceed 500 characters")
    private String description;

    private String failureReason;
    private Integer retryCount;
    private LocalDateTime initiatedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
