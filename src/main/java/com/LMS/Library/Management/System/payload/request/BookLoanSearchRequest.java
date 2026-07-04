package com.LMS.Library.Management.System.payload.request;

import com.LMS.Library.Management.System.domain.BookLoanStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookLoanSearchRequest {

    private Long userId;
    private Long bookId;
    private BookLoanStatus status;
    private Boolean overDueOnly;
    private Boolean unPaidFinesOnly;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
