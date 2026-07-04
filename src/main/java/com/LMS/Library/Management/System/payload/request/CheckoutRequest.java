package com.LMS.Library.Management.System.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutRequest {

    @NotNull(message = "book ID is mandatory")
    private Long bookId;

    @NotNull(message = "checkout days must be at least 1")
    private Integer checkoutDays=14;

    private String notes;
}
