package com.LMS.Library.Management.System.repository;

import com.LMS.Library.Management.System.domain.ReservationStatus;
import com.LMS.Library.Management.System.modal.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation,Long> {

    @Query("select case when count(r) > 0 then true else false end from Reservation r "+
            "where r.user.id = :userId AND r.book.id = :bookId "+
            "AND (r.status = 'PENDING' OR r.status = 'AVAILABLE')"
    )
    boolean hasActiveReservation(@Param("userId")Long userId, @Param("bookId")Long bookId);

    @Query("select count(r) from Reservation r where r.user.id = :userId "+
            "AND (r.status = 'PENDING' OR r.status = 'AVAILABLE')"
    )
    long countActiveReservationByUser(@Param("userId")Long userId);

    @Query("select count(r) from Reservation r where r.book.id = :bookId "+
            "AND r.status = 'PENDING'"
    )
    long countPendingReservationsByBook(@Param("bookId")Long bookId);


    @Query("select r from Reservation r where "+
            "(:userId is null OR r.user.id = :userId) AND "+
            "(:bookId is null OR r.book.id = :bookId) AND "+
            "(:status is null OR r.status = :status) AND "+
            "(:activeOnly = false OR (r.status = 'PENDING' OR r.status = 'AVAILABLE'))"
    )
    Page<Reservation> searchReservationWithFilters(
            @Param("userId")Long userId,
            @Param("bookId")Long bookId,
            @Param("status")ReservationStatus status,
            @Param("activeOnly")boolean activeOnly,
            Pageable pageable
            );
}
