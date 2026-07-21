package edu.ngd.controller;

import edu.ngd.dto.response.ApiResponse;
import edu.ngd.entity.OperationLog;
import edu.ngd.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/operation-logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OperationLogController {

    private final OperationLogService operationLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        Page<OperationLog> logPage;
        
        if (keyword != null && !keyword.isEmpty()) {
            logPage = operationLogService.searchLogs(keyword, page, size);
        } else if (startDate != null && endDate != null) {
            logPage = operationLogService.getLogsByDateRange(startDate, endDate, page, size);
        } else {
            logPage = operationLogService.getLogs(page, size);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("content", logPage.getContent());
        result.put("total", logPage.getTotalElements());
        result.put("totalPages", logPage.getTotalPages());
        result.put("page", logPage.getNumber());
        result.put("size", logPage.getSize());
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<OperationLog>>> getUserLogs(@PathVariable Long userId) {
        List<OperationLog> logs = operationLogService.getUserRecentLogs(userId);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalLogs", operationLogService.getTotalLogs());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}