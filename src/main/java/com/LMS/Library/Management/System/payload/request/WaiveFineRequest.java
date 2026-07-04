package com.LMS.Library.Management.System.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WaiveFineRequest {

    @NotNull(message = "fine ID is mandatory")
    private Long fineId;

    @NotBlank(message = "waive reason is mandatory")
    private String reason;
}
