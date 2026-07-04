package com.LMS.Library.Management.System.service.impl;

import com.LMS.Library.Management.System.domain.BookLoanStatus;
import com.LMS.Library.Management.System.mapper.BookReviewMapper;
import com.LMS.Library.Management.System.modal.Book;
import com.LMS.Library.Management.System.modal.BookLoan;
import com.LMS.Library.Management.System.modal.BookReview;
import com.LMS.Library.Management.System.modal.User;
import com.LMS.Library.Management.System.payload.dto.BookReviewDTO;
import com.LMS.Library.Management.System.payload.request.CreateReviewRequest;
import com.LMS.Library.Management.System.payload.request.UpdateReviewRequest;
import com.LMS.Library.Management.System.payload.response.PageResponse;
import com.LMS.Library.Management.System.repository.BookLoanRepository;
import com.LMS.Library.Management.System.repository.BookRepository;
import com.LMS.Library.Management.System.repository.BookReviewRepository;
import com.LMS.Library.Management.System.service.BookReviewService;
import com.LMS.Library.Management.System.service.UserService;
import jdk.jfr.Registered;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookReviewServiceImpl implements BookReviewService {

    private final BookReviewRepository bookReviewRepository;
    private final UserService userService;
    private final BookRepository bookRepository;
    private final BookReviewMapper bookReviewMapper;
    private final BookLoanRepository bookLoanRepository;
    @Override
    public BookReviewDTO createReview(CreateReviewRequest request) throws Exception {

        User user = userService.getCurrentUser();

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(()-> new Exception("book not found"));

        if(bookReviewRepository.existsByUserIdAndBookId(user.getId(),book.getId())){
            throw new Exception("you have already reviewed this book");
        }

        boolean hasReadBook = hasUserReadBook(user.getId(),book.getId());
        if(!hasReadBook){
            throw new Exception("you have not read this book");
        }

        BookReview bookReview = new BookReview();
        bookReview.setUser(user);
        bookReview.setBook(book);
        bookReview.setRating(request.getRating());
        bookReview.setReviewText(request.getReviewText());
        bookReview.setTitle(request.getTitle());

        BookReview savedBookReview = bookReviewRepository.save(bookReview);

        return bookReviewMapper.toDTO(savedBookReview);
    }

    @Override
    public BookReviewDTO updateReview(Long reviewId, UpdateReviewRequest request) throws Exception {

        User user = userService.getCurrentUser();

        BookReview bookReview = bookReviewRepository.findById(reviewId)
                .orElseThrow(()-> new Exception("review not found"));

        if(!bookReview.getUser().getId().equals(user.getId())){
            throw new Exception("you have not reviewed this book");
        }

        bookReview.setReviewText(request.getReviewText());
        bookReview.setTitle(request.getTitle());
        bookReview.setRating(request.getRating());

        BookReview savedBookReview = bookReviewRepository.save(bookReview);

        return bookReviewMapper.toDTO(savedBookReview);
    }

    @Override
    public void deleteReview(Long reviewId) throws Exception {

        User user = userService.getCurrentUser();

        BookReview bookReview = bookReviewRepository.findById(reviewId)
                .orElseThrow(()-> new Exception("review not found"));

        if(!bookReview.getUser().getId().equals(user.getId())){
            throw new Exception("you can only delete your own reviews");
        }

        bookReviewRepository.delete(bookReview);
    }

    @Override
    public PageResponse<BookReviewDTO> getReviewsByBookId(Long id, int page, int size) throws Exception {

        Book book = bookRepository.findById(id)
                .orElseThrow(()-> new Exception("book not found"));

        Pageable pageable = PageRequest.of(page,size, Sort.by("createdAt").descending());
        Page<BookReview> reviewPage = bookReviewRepository.findByBook(book,pageable);
        return convertToPageResponse(reviewPage);
    }

    private PageResponse<BookReviewDTO> convertToPageResponse(Page<BookReview> reviewPage) {
        List<BookReviewDTO> reviewDTOs = reviewPage.getContent()
                .stream()
                .map(bookReviewMapper::toDTO)
                .collect(Collectors.toList());

        return new PageResponse<>(
                reviewDTOs,
                reviewPage.getNumber(),
                reviewPage.getSize(),
                reviewPage.getTotalElements(),
                reviewPage.getTotalPages(),
                reviewPage.isLast(),
                reviewPage.isFirst(),
                reviewPage.isEmpty()
        );
    }

    private boolean hasUserReadBook(Long userId,Long bookId){
        List<BookLoan> bookLoans = bookLoanRepository.findByBookId(bookId);
        return bookLoans.stream()
                .anyMatch(loan->loan.getUser().getId().equals(userId) &&
                        loan.getStatus() == BookLoanStatus.RETURNED);
    }
}
