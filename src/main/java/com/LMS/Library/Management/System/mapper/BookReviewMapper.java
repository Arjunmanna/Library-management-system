package com.LMS.Library.Management.System.mapper;

import com.LMS.Library.Management.System.modal.BookReview;
import com.LMS.Library.Management.System.payload.dto.BookReviewDTO;
import jakarta.persistence.Column;
import org.springframework.stereotype.Component;

@Component
public class BookReviewMapper {

    public BookReviewDTO toDTO (BookReview bookReview) {
        if (bookReview == null) {
            return null;
        }

        return BookReviewDTO.builder()
                .id(bookReview.getId())
                .userId(bookReview.getUser().getId())
                .userName(bookReview.getUser().getFullName())
                .bookId(bookReview.getBook().getId())
                .bookTitle(bookReview.getBook().getTitle())
                .rating(bookReview.getRating())
                .reviewText(bookReview.getReviewText())
                .title(bookReview.getTitle())
                .createdAt(bookReview.getCreatedAt())
                .updatedAt(bookReview.getUpdatedAt())
                .build();
    }
}
