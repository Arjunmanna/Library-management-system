package com.LMS.Library.Management.System.service.impl;

import com.LMS.Library.Management.System.domain.BookLoanStatus;
import com.LMS.Library.Management.System.domain.BookLoanType;
import com.LMS.Library.Management.System.exception.BookException;
import com.LMS.Library.Management.System.mapper.BookLoanMapper;
import com.LMS.Library.Management.System.modal.Book;
import com.LMS.Library.Management.System.modal.BookLoan;
import com.LMS.Library.Management.System.modal.User;
import com.LMS.Library.Management.System.payload.dto.BookLoanDTO;
import com.LMS.Library.Management.System.payload.dto.SubscriptionDTO;
import com.LMS.Library.Management.System.payload.request.BookLoanSearchRequest;
import com.LMS.Library.Management.System.payload.request.CheckinRequest;
import com.LMS.Library.Management.System.payload.request.CheckoutRequest;
import com.LMS.Library.Management.System.payload.request.RenewalRequset;
import com.LMS.Library.Management.System.payload.response.PageResponse;
import com.LMS.Library.Management.System.repository.BookLoanRepository;
import com.LMS.Library.Management.System.repository.BookRepository;
import com.LMS.Library.Management.System.service.BookLoanService;
import com.LMS.Library.Management.System.service.SubscriptionService;
import com.LMS.Library.Management.System.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookLoanServiceImpl implements BookLoanService {

    private final BookLoanRepository bookLoanRepository;
    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final BookRepository bookRepository;
    private final BookLoanMapper bookLoanMapper;

    @Override
    public BookLoanDTO checkoutBook(CheckoutRequest checkoutRequest) throws Exception {

        User user = userService.getCurrentUser();
        return checkoutBookForUser(user.getId(),checkoutRequest);
    }

    @Override
    public BookLoanDTO checkoutBookForUser(Long userId, CheckoutRequest checkoutRequest) throws Exception {
        User user = userService.findById(userId);
        SubscriptionDTO subscription = subscriptionService
                .getUsersActiveSubscription(user.getId());

        Book book = bookRepository.findById(checkoutRequest.getBookId())
                .orElseThrow(()-> new BookException("book not found with id: "+ checkoutRequest.getBookId()));

        if(!book.getActive()){
            throw new BookException("book is not active");
        }
        if(book.getAvailableCopies()<=0){
            throw new BookException("book is not available");
        }

        if(bookLoanRepository.hasActiveCheckout(userId,book.getId())){
            throw new BookException("book already has active checkout");
        }

        long activeCheckouts = bookLoanRepository.countActiveBookLoansByUser(userId);
        int maxBooksAllowed = subscription.getMaxBooksAllowed();

        if(activeCheckouts>=maxBooksAllowed){
            throw new Exception("you have reached your maximum number of books allowed");
        }

        long overDueCount = bookLoanRepository.countOverDueBookLoansByUser(userId);
        if(overDueCount>0){
            throw new Exception("first return old overdue book");
        }

        BookLoan bookLoan = BookLoan.builder()
                .user(user)
                .book(book)
                .type(BookLoanType.CHECKOUT)
                .status(BookLoanStatus.CHECKED_OUT)
                .checkoutDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(checkoutRequest.getCheckoutDays()))
                .renewalCount(0)
                .maxRenewals(20)
                .notes(checkoutRequest.getNotes())
                .isOverDue(false)
                .overDueDays(0)
                .build();

        book.setAvailableCopies(book.getAvailableCopies()-1);
        bookRepository.save(book);

        BookLoan savedBookLoan = bookLoanRepository.save(bookLoan);
        return bookLoanMapper.toDTO(savedBookLoan);
    }

    @Override
    public BookLoanDTO checkinBook(CheckinRequest checkinRequest)throws Exception {

        BookLoan bookLoan = bookLoanRepository.findById(checkinRequest.getBookLoanId())
                .orElseThrow(()-> new Exception("bookLoan not found"));

        if(!bookLoan.isActive()){
            throw new BookException("bookLoan is not active");
        }

        bookLoan.setReturnDate(LocalDate.now());

        BookLoanStatus condition = checkinRequest.getCondition();
        if(condition==null) {
            condition=BookLoanStatus.RETURNED;
        }
        bookLoan.setStatus(condition);

        bookLoan.setOverDueDays(0);
        bookLoan.setIsOverDue(false);

        bookLoan.setNotes("book returned by user");

        if(condition != BookLoanStatus.LOST){
            Book book = bookLoan.getBook();
            book.setAvailableCopies(book.getAvailableCopies()+1);
            bookRepository.save(book);
        }

        BookLoan savedBookLoan = bookLoanRepository.save(bookLoan);

        return bookLoanMapper.toDTO(savedBookLoan);
    }

    @Override
    public BookLoanDTO renewCheckout(RenewalRequset renewalRequset)throws Exception {

        BookLoan bookLoan = bookLoanRepository.findById(renewalRequset.getBookLoanId())
                .orElseThrow(()-> new Exception("bookLoan not found"));

        if(!bookLoan.canRenew()){
            throw new BookException("book cannot be renewed");
        }

        bookLoan.setDueDate(bookLoan.getDueDate()
                .plusDays(renewalRequset.getExtensionDays()));

        bookLoan.setRenewalCount(bookLoan.getRenewalCount()+1);

        bookLoan.setNotes("book renewed by user");

        BookLoan savedBookLoan = bookLoanRepository.save(bookLoan);

        return bookLoanMapper.toDTO(savedBookLoan);
    }

    @Override
    public PageResponse<BookLoanDTO> getMyBookLoans(BookLoanStatus status, int page, int size) throws Exception {

        User currentUser = userService.getCurrentUser();
        Page<BookLoan> bookLoanPage;

        if (status!=null) {
            Pageable pageable = PageRequest.of(page, size, Sort.by( "dueDate").ascending());
            bookLoanPage = bookLoanRepository.findByStatusAndUser(status, currentUser, pageable);
        } else {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            bookLoanPage = bookLoanRepository.findByUserId(currentUser.getId(), pageable);
        }

        return convertToPageResponse (bookLoanPage);
    }

    @Override
    public PageResponse<BookLoanDTO> getBookLoans(BookLoanSearchRequest searchRequest) {

        Pageable pageable = createPageable(
                searchRequest.getPage(),
                searchRequest.getSize(),
                searchRequest.getSortBy(),
                searchRequest.getSortDirection()
        );
        Page<BookLoan> bookLoanPage;

        if(Boolean.TRUE.equals(searchRequest.getOverDueOnly())){
            bookLoanPage = bookLoanRepository.findOverDueBookLoans(LocalDate.now(),pageable);
        } else if (searchRequest.getUserId() != null) {
            bookLoanPage = bookLoanRepository.findByUserId(searchRequest.getUserId(),pageable);
        } else if (searchRequest.getBookId() != null) {
            bookLoanPage = bookLoanRepository.findByBookId(searchRequest.getBookId(),pageable);
        } else if (searchRequest.getStatus() != null) {
            bookLoanPage = bookLoanRepository.findByStatus(searchRequest.getStatus(),pageable);
        } else if (searchRequest.getStartDate() != null) {
            bookLoanPage = bookLoanRepository.findBookLoansByDateRange(
                    searchRequest.getStartDate(),
                    searchRequest.getEndDate(),
                    pageable
            );
        }else {
            bookLoanPage = bookLoanRepository.findAll(pageable);
        }

        return convertToPageResponse(bookLoanPage);
    }

    @Override
    public int updateOverDueBookLoan() {

        Pageable pageable = PageRequest.of(0,1000);
        Page<BookLoan> overDuePage = bookLoanRepository
                .findOverDueBookLoans(LocalDate.now(),pageable);

        int updateCount = 0;
        for(BookLoan bookLoan : overDuePage.getContent()){
            if(bookLoan.getStatus() == BookLoanStatus.CHECKED_OUT){
                bookLoan.setStatus(BookLoanStatus.OVERDUE);
                bookLoan.setIsOverDue(true);

                int overDueDays = calculateOverDueDate(
                        bookLoan.getDueDate(),
                        LocalDate.now()
                );

                bookLoanRepository.save(bookLoan);
                updateCount++;
            }
        }
        return updateCount;
    }

    private Pageable createPageable(int page,int size,String sortBy,String sortDirection){
        size = Math.min(size,100);
        size = Math.max(size,1);

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        return PageRequest.of(page,size,sort);
    }

    private PageResponse<BookLoanDTO> convertToPageResponse(Page<BookLoan> bookLoanPage){
        List<BookLoanDTO> bookLoanDTOs = bookLoanPage.getContent()
                .stream().map(bookLoanMapper::toDTO)
                .collect(Collectors.toList());

        return new PageResponse<>(
                bookLoanDTOs,
                bookLoanPage.getNumber(),
                bookLoanPage.getSize(),
                bookLoanPage.getTotalElements(),
                bookLoanPage.getTotalPages(),
                bookLoanPage.isLast(),
                bookLoanPage.isFirst(),
                bookLoanPage.isEmpty()
        );
    }

    public int calculateOverDueDate(LocalDate dueDate,LocalDate today){
        if(today.isBefore(dueDate) || today.isEqual(dueDate)){
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(dueDate,today);
    }
}
