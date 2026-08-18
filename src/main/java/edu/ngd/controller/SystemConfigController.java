package edu.ngd.controller;

import edu.ngd.dto.response.ApiResponse;
import edu.ngd.entity.SystemConfig;
import edu.ngd.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/system-configs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SystemConfig>>> getAllConfigs() {
        List<SystemConfig> configs = systemConfigService.getAllConfigs();
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<SystemConfig>> getConfigByKey(@PathVariable String key) {
        SystemConfig config = systemConfigService.getConfig(key);
        if (config == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "配置不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SystemConfig>> createConfig(@RequestBody Map<String, String> request) {
        String key = request.get("configKey");
        String value = request.get("configValue");
        String description = request.get("description");
        SystemConfig config = systemConfigService.createConfig(key, value, description);
        return ResponseEntity.ok(ApiResponse.success("配置创建成功", config));
    }

    @PutMapping("/{key}")
    public ResponseEntity<ApiResponse<SystemConfig>> updateConfig(
            @PathVariable String key,
            @RequestBody Map<String, String> request) {
        String value = request.get("configValue");
        String description = request.get("description");
        SystemConfig config = systemConfigService.updateConfig(key, value, description);
        return ResponseEntity.ok(ApiResponse.success("配置更新成功", config));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<ApiResponse<Void>> deleteConfig(@PathVariable String key) {
        systemConfigService.deleteConfig(key);
        return ResponseEntity.ok(ApiResponse.success("配置删除成功"));
    }
}