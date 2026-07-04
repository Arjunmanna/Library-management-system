package com.LMS.Library.Management.System.repository;

import com.LMS.Library.Management.System.domain.FineStatus;
import com.LMS.Library.Management.System.domain.FineType;
import com.LMS.Library.Management.System.modal.Fine;
import jakarta.validation.constraints.Future;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FineRepository extends JpaRepository<Fine,Long> {

    @Query("""
            select f from Fine f
            where (:userId is null OR f.user.id = :userId)
            And (:status is null OR f.status = :status)
            AND (:type is null OR f.type = :type)
            order by f.createdAt DESC
            """)
    Page<Fine> findAllWithFilters(
            @Param("userId") Long userId,
            @Param("status")FineStatus status,
            @Param("type")FineType type,
            Pageable pageable
            );

    List<Fine> findByUserId(Long userId);

    List<Fine> findByUserIdAndType(Long userId,FineType type);
}
