package com.LMS.Library.Management.System.service.impl;

import com.LMS.Library.Management.System.domain.FineStatus;
import com.LMS.Library.Management.System.domain.FineType;
import com.LMS.Library.Management.System.domain.PaymentGateway;
import com.LMS.Library.Management.System.domain.PaymentType;
import com.LMS.Library.Management.System.mapper.FineMapper;
import com.LMS.Library.Management.System.modal.BookLoan;
import com.LMS.Library.Management.System.modal.Fine;
import com.LMS.Library.Management.System.modal.User;
import com.LMS.Library.Management.System.payload.dto.FineDTO;
import com.LMS.Library.Management.System.payload.request.CreateFineRequest;
import com.LMS.Library.Management.System.payload.request.PaymentInitiateRequest;
import com.LMS.Library.Management.System.payload.request.WaiveFineRequest;
import com.LMS.Library.Management.System.payload.response.PageResponse;
import com.LMS.Library.Management.System.payload.response.PaymentInitiateResponse;
import com.LMS.Library.Management.System.repository.BookLoanRepository;
import com.LMS.Library.Management.System.repository.FineRepository;
import com.LMS.Library.Management.System.service.FineService;
import com.LMS.Library.Management.System.service.PaymentService;
import com.LMS.Library.Management.System.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FineServiceImpl implements FineService {

    private final BookLoanRepository bookLoanRepository;
    private final FineRepository fineRepository;
    private final FineMapper fineMapper;
    private final UserService userService;
    private final PaymentService paymentService;

    @Override
    public FineDTO createFine(CreateFineRequest createFineRequest) throws Exception{

        BookLoan bookLoan = bookLoanRepository.findById(createFineRequest.getBookLoanId())
                .orElseThrow(()-> new Exception("book loan does not exist"));

        Fine fine = Fine.builder()
                .bookLoan(bookLoan)
                .user(bookLoan.getUser())
                .type(createFineRequest.getType())
                .amount(createFineRequest.getAmount())
                .status(FineStatus.PENDING)
                .reason(createFineRequest.getReason())
                .notes(createFineRequest.getNotes())
                .build();

        Fine savedFine = fineRepository.save(fine);
        return fineMapper.toDTO(savedFine);
    }

    @Override
    public PaymentInitiateResponse payFine(Long fineId, String transactionId) throws Exception {

        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(()-> new Exception("fine does not exist"));

        if(fine.getStatus().equals(FineStatus.PAID)){
            throw new Exception("fine already paid");
        }
        if(fine.getStatus().equals(FineStatus.WAIVED)){
            throw new Exception("fine waived");
        }

        User user = userService.getCurrentUser();

        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .userId(user.getId())
                .fineId(fine.getId())
                .paymentType(PaymentType.FINE)
                .gateway(PaymentGateway.RAZORPAY)
                .amount(fine.getAmount())
                .description("library fine payment")
                .build();
        return paymentService.initiatePayment(request);
    }

    @Override
    public void markFineAsPaid(Long fineId, Long amount, String transactionId) throws Exception {

        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(()-> new Exception("fine not found with id: "+fineId));

        fine.applyPayment(amount);
        fine.setTransactionId(transactionId);
        fine.setStatus(FineStatus.PAID);
        fine.setUpdatedAt(LocalDateTime.now());

        fineRepository.save(fine);
    }

    @Override
    public FineDTO waiveFine(WaiveFineRequest waiveFineRequest) throws Exception {

        Fine fine = fineRepository.findById(waiveFineRequest.getFineId())
                .orElseThrow(()-> new Exception("fine not found"));

        if(fine.getStatus() == FineStatus.WAIVED){
            throw new Exception("fine has already been waived");
        }
        if(fine.getStatus() == FineStatus.PAID){
            throw new Exception("fine has already been paid and can not be waived");
        }

        User currentAdmin = userService.getCurrentUser();
        fine.waive(currentAdmin,waiveFineRequest.getReason());

        Fine savedFine = fineRepository.save(fine);

        return fineMapper.toDTO(savedFine);
    }

    @Override
    public List<FineDTO> getMyFines(FineStatus status, FineType type) throws Exception {

        User currentUser = userService.getCurrentUser();
        List<Fine> fines;

        if(status != null && type != null){
            fines = fineRepository.findByUserId(currentUser.getId())
                    .stream()
                    .filter(f -> f.getStatus()==status && f.getType() == type)
                    .collect(Collectors.toList());
        } else if (status != null) {
            fines = fineRepository.findByUserId(currentUser.getId())
                    .stream()
                    .filter(f -> f.getStatus()==status)
                    .collect(Collectors.toList());
        } else if (type != null) {
            fines = fineRepository.findByUserIdAndType(currentUser.getId(),type);
        }else {
            fines = fineRepository.findByUserId(currentUser.getId());
        }

        return fines.stream().map(
                fineMapper::toDTO
        ).collect(Collectors.toList());
    }

    @Override
    public PageResponse<FineDTO> getAllFines(FineStatus status, FineType type, Long userId, int page, int size) {

        Pageable pageable = PageRequest.of(page,size, Sort.by("createdAt").descending());

        Page<Fine> finePage = fineRepository.findAllWithFilters(
                userId,
                status,
                type,
                pageable
        );
        return convertToPageResponse(finePage);
    }

    private PageResponse<FineDTO> convertToPageResponse(Page<Fine> finePage){
        List<FineDTO> fineDTOs = finePage.getContent()
                .stream()
                .map(fineMapper::toDTO)
                .collect(Collectors.toList());

        return new PageResponse<>(
                fineDTOs,
                finePage.getNumber(),
                finePage.getSize(),
                finePage.getTotalElements(),
                finePage.getTotalPages(),
                finePage.isLast(),
                finePage.isFirst(),
                finePage.isEmpty()
        );
    }
}
