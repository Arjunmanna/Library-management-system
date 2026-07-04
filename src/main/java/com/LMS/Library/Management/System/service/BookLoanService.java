package com.LMS.Library.Management.System.service;

import com.LMS.Library.Management.System.domain.BookLoanStatus;
import com.LMS.Library.Management.System.payload.dto.BookLoanDTO;
import com.LMS.Library.Management.System.payload.request.BookLoanSearchRequest;
import com.LMS.Library.Management.System.payload.request.CheckinRequest;
import com.LMS.Library.Management.System.payload.request.CheckoutRequest;
import com.LMS.Library.Management.System.payload.request.RenewalRequset;
import com.LMS.Library.Management.System.payload.response.PageResponse;

public interface BookLoanService {

    BookLoanDTO checkoutBook(CheckoutRequest checkoutRequest) throws Exception;
    BookLoanDTO checkoutBookForUser(Long userId,CheckoutRequest checkoutRequest) throws Exception;
    BookLoanDTO checkinBook(CheckinRequest checkinRequest)throws Exception;
    BookLoanDTO renewCheckout(RenewalRequset renewalRequset)throws Exception;
    PageResponse<BookLoanDTO> getMyBookLoans(BookLoanStatus status,int page,int size) throws Exception;
    PageResponse<BookLoanDTO> getBookLoans(BookLoanSearchRequest req);
    int updateOverDueBookLoan();
}
