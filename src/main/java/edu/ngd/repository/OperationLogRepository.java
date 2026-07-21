package edu.ngd.repository;

import edu.ngd.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
    Page<OperationLog> findByUserId(Long userId, Pageable pageable);
    Page<OperationLog> findByAction(String action, Pageable pageable);
    Page<OperationLog> findByTargetType(String targetType, Pageable pageable);
    Page<OperationLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    Page<OperationLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<OperationLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    Page<OperationLog> findByActionContainingOrDetailsContaining(String action, String details, Pageable pageable);
}