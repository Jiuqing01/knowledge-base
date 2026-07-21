package edu.ngd.service;

import edu.ngd.entity.OperationLog;
import edu.ngd.repository.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogRepository operationLogRepository;

    @Transactional
    public OperationLog log(Long userId, String action, String targetType, 
                            Long targetId, String details, String ipAddress) {
        OperationLog logEntry = OperationLog.builder()
                .userId(userId)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .details(details)
                .ipAddress(ipAddress)
                .build();
        
        OperationLog saved = operationLogRepository.save(logEntry);
        log.info("Operation logged: userId={}, action={}, targetType={}, targetId={}", 
                userId, action, targetType, targetId);
        return saved;
    }

    @Transactional
    public OperationLog log(Long userId, String action, String targetType, 
                            Long targetId, String details) {
        return log(userId, action, targetType, targetId, details, null);
    }

    public Page<OperationLog> getLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return operationLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public Page<OperationLog> getLogsByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return operationLogRepository.findByUserId(userId, pageable);
    }

    public Page<OperationLog> getLogsByAction(String action, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return operationLogRepository.findByAction(action, pageable);
    }

    public Page<OperationLog> searchLogs(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return operationLogRepository.findByActionContainingOrDetailsContaining(keyword, keyword, pageable);
    }

    public Page<OperationLog> getLogsByTargetType(String targetType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return operationLogRepository.findByTargetType(targetType, pageable);
    }

    public Page<OperationLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return operationLogRepository.findByCreatedAtBetween(start, end, pageable);
    }

    public List<OperationLog> getUserRecentLogs(Long userId) {
        return operationLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public long getTotalLogs() {
        return operationLogRepository.count();
    }

    @Transactional
    public void deleteOldLogs(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        List<OperationLog> oldLogs = operationLogRepository.findAll().stream()
                .filter(log -> log.getCreatedAt().isBefore(cutoffDate))
                .collect(java.util.stream.Collectors.toList());
        
        operationLogRepository.deleteAll(oldLogs);
        log.info("Deleted {} old logs older than {} days", oldLogs.size(), daysToKeep);
    }
}