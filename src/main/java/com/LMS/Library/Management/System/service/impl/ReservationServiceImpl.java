package com.LMS.Library.Management.System.service.impl;

import com.LMS.Library.Management.System.domain.BookLoanStatus;
import com.LMS.Library.Management.System.domain.ReservationStatus;
import com.LMS.Library.Management.System.domain.UserRole;
import com.LMS.Library.Management.System.mapper.ReservationMapper;
import com.LMS.Library.Management.System.modal.Book;
import com.LMS.Library.Management.System.modal.Reservation;
import com.LMS.Library.Management.System.modal.User;
import com.LMS.Library.Management.System.payload.dto.ReservationDTO;
import com.LMS.Library.Management.System.payload.request.CheckoutRequest;
import com.LMS.Library.Management.System.payload.request.ReservationRequest;
import com.LMS.Library.Management.System.payload.request.ReservationSearchRequest;
import com.LMS.Library.Management.System.payload.response.PageResponse;
import com.LMS.Library.Management.System.repository.BookLoanRepository;
import com.LMS.Library.Management.System.repository.BookRepository;
import com.LMS.Library.Management.System.repository.ReservationRepository;
import com.LMS.Library.Management.System.service.BookLoanService;
import com.LMS.Library.Management.System.service.ReservationService;
import com.LMS.Library.Management.System.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final BookRepository bookRepository;
    private final BookLoanRepository bookLoanRepository;
    private final UserService userService;
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final BookLoanService bookLoanService;

    int MAX_RESERVATIONS = 5;

    @Override
    public ReservationDTO createReservation(ReservationRequest reservationRequest) throws Exception {
        User user = userService.getCurrentUser();

        return createReservationForUser(reservationRequest,user.getId());
    }

    @Override
    public ReservationDTO createReservationForUser(ReservationRequest reservationRequest, Long userId) throws Exception {
        boolean alreadyHasLoan = bookLoanRepository.existsByUserIdAndBookIdAndStatus(
                userId,reservationRequest.getBookId(), BookLoanStatus.CHECKED_OUT
        );

        if(alreadyHasLoan){
            throw new Exception("you already have loan on this book");
        }

        User user = userService.getCurrentUser();

        Book book = bookRepository.findById(reservationRequest.getBookId())
                .orElseThrow(()-> new Exception("book not found"));

        if(reservationRepository.hasActiveReservation(userId,book.getId())){
            throw new Exception("you have already reservation on this book");
        }

        if(book.getAvailableCopies()>0){
            throw new Exception("book is already available");
        }

        long activeReservations = reservationRepository
                .countActiveReservationByUser(userId);

        if(activeReservations>=MAX_RESERVATIONS){
            throw new Exception("you have reserved "+MAX_RESERVATIONS+" times");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setReservedAt(LocalDateTime.now());
        reservation.setNotificationSent(false);
        reservation.setNotes(reservationRequest.getNotes());

        long pendingCount = reservationRepository.countPendingReservationsByBook(book.getId());
        reservation.setQueuePosition((int)pendingCount+1);
        Reservation savedReservation = reservationRepository.save(reservation);

        return reservationMapper.toDTO(savedReservation);
    }

    @Override
    public ReservationDTO cancelReservation(Long reservationId) throws Exception {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(()-> new Exception("reservation is not found with ID: "+reservationId));

        User currentUser = userService.getCurrentUser();
        if(!reservation.getUser().getId().equals(currentUser.getId()) && currentUser.getRole()!= UserRole.ROLE_ADMIN){
            throw new Exception("you can only cancel your own reservations");
        }
        if(!reservation.canBeCancelled()){
            throw new Exception("reservation can not be cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());

        Reservation savedReservation = reservationRepository.save(reservation);

        return reservationMapper.toDTO(savedReservation);
    }

    @Override
    public ReservationDTO fulfillReservation(Long reservationId) throws Exception {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(()-> new Exception("reservation is not found with ID: "+reservationId));

        if(reservation.getBook().getAvailableCopies()<=0){
            throw new Exception("reservation is not available for pickup");
        }

        reservation.setStatus(ReservationStatus.FULFILLED);
        reservation.setFulfilledAt(LocalDateTime.now());

        Reservation savedReservation = reservationRepository.save(reservation);

        CheckoutRequest request = new CheckoutRequest();
        request.setBookId(reservation.getBook().getId());
        request.setNotes("assign booked by admin");

        bookLoanService.checkoutBookForUser(reservation.getUser().getId(),request);

        return reservationMapper.toDTO(savedReservation);
    }

    @Override
    public PageResponse<ReservationDTO> getMyReservations(ReservationSearchRequest searchRequest) throws Exception {
        User user = userService.getCurrentUser();
        searchRequest.setUserId(user.getId());
        return searchReservations(searchRequest);
    }

    @Override
    public PageResponse<ReservationDTO> searchReservations(ReservationSearchRequest searchRequest) {
        Pageable pageable = createPageable(searchRequest);
        Page<Reservation> reservationPage = reservationRepository.searchReservationWithFilters(
                searchRequest.getUserId(),
                searchRequest.getBookId(),
                searchRequest.getStatus(),
                searchRequest.getActiveOnly() != null ? searchRequest.getActiveOnly() : false,
                pageable
        );
        return buildPageResponse(reservationPage);
    }

    private PageResponse<ReservationDTO> buildPageResponse(Page<Reservation> reservationPage){
        List<ReservationDTO> dtos = reservationPage.getContent().stream()
                .map(reservationMapper::toDTO)
                .toList();

        PageResponse<ReservationDTO> response = new PageResponse<>();
        response.setContent(dtos);
        response.setPageNumber(reservationPage.getNumber());
        response.setPageSize(reservationPage.getSize());
        response.setTotalElements(reservationPage.getTotalElements());
        response.setTotalPages(reservationPage.getTotalPages());
        response.setLast(reservationPage.isLast());

        return response;
    }

    private Pageable createPageable(ReservationSearchRequest searchRequest){
        Sort sort = "ASC".equalsIgnoreCase(searchRequest.getSortDirection())
                ? Sort.by(searchRequest.getSortBy()).ascending()
                : Sort.by(searchRequest.getSortBy()).descending();

        return PageRequest.of(searchRequest.getPage(),searchRequest.getSize(),sort);
    }
}
