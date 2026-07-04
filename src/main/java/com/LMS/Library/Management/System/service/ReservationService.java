package com.LMS.Library.Management.System.service;

import com.LMS.Library.Management.System.payload.dto.ReservationDTO;
import com.LMS.Library.Management.System.payload.request.ReservationRequest;
import com.LMS.Library.Management.System.payload.request.ReservationSearchRequest;
import com.LMS.Library.Management.System.payload.response.PageResponse;

public interface ReservationService {

    ReservationDTO createReservation(ReservationRequest reservationRequest) throws Exception;

    ReservationDTO createReservationForUser(ReservationRequest reservationRequest,Long userId) throws Exception;

    ReservationDTO cancelReservation(Long reservationId) throws Exception;

    ReservationDTO fulfillReservation(Long reservationId) throws Exception;

    PageResponse<ReservationDTO> getMyReservations(ReservationSearchRequest searchRequest) throws Exception;
    PageResponse<ReservationDTO> searchReservations(ReservationSearchRequest searchRequest);

}
