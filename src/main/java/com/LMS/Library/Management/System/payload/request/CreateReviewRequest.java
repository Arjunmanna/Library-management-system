package com.LMS.Library.Management.System.payload.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateReviewRequest {

    @NotNull(message = "book ID is mandatory")
    private Long bookId;

    @NotNull(message = "rating is mandatory")
    @Min(value = 1,message = "rating must be at least 1")
    @Max(value = 5,message = "rating must not exceed 5")
    private Integer rating;

    @NotBlank(message = "review text is mandatory")
    @Size(min = 10,max = 2000,message = "review must be between 10 and 2000 characters")
    private String reviewText;

    @Size(max = 200,message = "review title must not exceed 200 characters")
    private String title;
}
