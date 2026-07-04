package com.LMS.Library.Management.System.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservationRequest {

    @NotNull(message = "book ID is mandatory")
    private Long bookId;

    private String notes;
}
