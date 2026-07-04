package com.LMS.Library.Management.System.service;

import com.LMS.Library.Management.System.domain.FineStatus;
import com.LMS.Library.Management.System.domain.FineType;
import com.LMS.Library.Management.System.payload.dto.FineDTO;
import com.LMS.Library.Management.System.payload.request.CreateFineRequest;
import com.LMS.Library.Management.System.payload.request.WaiveFineRequest;
import com.LMS.Library.Management.System.payload.response.PageResponse;
import com.LMS.Library.Management.System.payload.response.PaymentInitiateResponse;

import java.util.List;

public interface FineService {

    FineDTO createFine(CreateFineRequest createFineRequest)throws Exception;

    PaymentInitiateResponse payFine(Long fineId,String transactionId) throws Exception;

    void markFineAsPaid(Long fineId,Long amount,String transactionId) throws Exception;

    FineDTO waiveFine(WaiveFineRequest waiveFineRequest) throws Exception;

    List<FineDTO> getMyFines(FineStatus status, FineType type) throws Exception;

    PageResponse<FineDTO> getAllFines(
            FineStatus status,
            FineType type,
            Long userId,
            int page,
            int size
    );
}
