package com.LMS.Library.Management.System.controller;

import com.LMS.Library.Management.System.domain.FineStatus;
import com.LMS.Library.Management.System.domain.FineType;
import com.LMS.Library.Management.System.payload.dto.FineDTO;
import com.LMS.Library.Management.System.payload.request.CreateFineRequest;
import com.LMS.Library.Management.System.payload.request.WaiveFineRequest;
import com.LMS.Library.Management.System.payload.response.PageResponse;
import com.LMS.Library.Management.System.payload.response.PaymentInitiateResponse;
import com.LMS.Library.Management.System.service.FineService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fines")
public class FineController {

    private final FineService fineService;

    @PostMapping
    public ResponseEntity<?> createFine(@Valid @RequestBody CreateFineRequest fineRequest) throws Exception {
        FineDTO fineDTO = fineService.createFine(fineRequest);

        return ResponseEntity.ok(fineDTO);
    }

    @PostMapping("{id}/pay")
    public ResponseEntity<?> payFine(@PathVariable Long id, @RequestParam(required = false) String transactionId) throws Exception {

        PaymentInitiateResponse response = fineService.payFine(id,transactionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/waive")
    public ResponseEntity<?> waiveFine(@Valid @RequestBody WaiveFineRequest fineRequest) throws Exception {
        FineDTO fineDTO = fineService.waiveFine(fineRequest);

        return ResponseEntity.ok(fineDTO);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyFines(
            @RequestParam(required = false)FineStatus status,
            @RequestParam(required = false)FineType type
            ) throws Exception {
        List<FineDTO> fines = fineService.getMyFines(status, type);

        return ResponseEntity.ok(fines);
    }

    @GetMapping
    public ResponseEntity<?> getAllFines(
            @RequestParam(required = false)FineStatus status,
            @RequestParam(required = false)FineType type,
            @RequestParam(required = false)Long userId,
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "20")int size
    ) throws Exception {
        PageResponse<FineDTO> fines = fineService
                .getAllFines(status, type, userId, page, size);

        return ResponseEntity.ok(fines);
    }
}
