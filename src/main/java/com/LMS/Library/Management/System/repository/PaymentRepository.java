package com.LMS.Library.Management.System.repository;

import com.LMS.Library.Management.System.modal.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
}
