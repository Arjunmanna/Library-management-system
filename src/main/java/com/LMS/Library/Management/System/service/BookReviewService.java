package com.LMS.Library.Management.System.service;

import com.LMS.Library.Management.System.payload.dto.BookReviewDTO;
import com.LMS.Library.Management.System.payload.request.CreateReviewRequest;
import com.LMS.Library.Management.System.payload.request.UpdateReviewRequest;
import com.LMS.Library.Management.System.payload.response.PageResponse;

public interface BookReviewService {

    BookReviewDTO createReview(CreateReviewRequest request) throws Exception;
    BookReviewDTO updateReview(Long reviewId, UpdateReviewRequest request) throws Exception;
    void deleteReview(Long reviewId) throws Exception;
    PageResponse<BookReviewDTO> getReviewsByBookId(Long id,int page,int size) throws Exception;
}
