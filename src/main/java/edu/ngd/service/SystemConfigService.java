package edu.ngd.service;

import edu.ngd.entity.SystemConfig;
import edu.ngd.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

    public SystemConfig getConfig(String key) {
        return systemConfigRepository.findByConfigKey(key).orElse(null);
    }

    public String getConfigValue(String key) {
        return systemConfigRepository.findByConfigKey(key)
                .map(SystemConfig::getConfigValue)
                .orElse(null);
    }

    public String getConfigValue(String key, String defaultValue) {
        return systemConfigRepository.findByConfigKey(key)
                .map(SystemConfig::getConfigValue)
                .orElse(defaultValue);
    }

    @Transactional
    public SystemConfig updateConfig(String key, String value, String description) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
                .orElse(new SystemConfig());
        config.setConfigKey(key);
        config.setConfigValue(value);
        if (description != null) {
            config.setDescription(description);
        }
        SystemConfig saved = systemConfigRepository.save(config);
        log.info("Updated system config: key={}, value={}", key, value);
        return saved;
    }

    @Transactional
    public void deleteConfig(String key) {
        if (!systemConfigRepository.existsByConfigKey(key)) {
            throw new RuntimeException("配置不存在");
        }
        systemConfigRepository.deleteByConfigKey(key);
        log.info("Deleted system config: key={}", key);
    }

    public List<SystemConfig> getAllConfigs() {
        return systemConfigRepository.findAll();
    }

    @Transactional
    public SystemConfig createConfig(String key, String value, String description) {
        if (systemConfigRepository.existsByConfigKey(key)) {
            throw new RuntimeException("配置已存在");
        }
        SystemConfig config = new SystemConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setDescription(description);
        SystemConfig saved = systemConfigRepository.save(config);
        log.info("Created system config: key={}, value={}", key, value);
        return saved;
    }

    public Long getMaxFileSize() {
        String value = getConfigValue("max_file_size", "524288000");
        return Long.parseLong(value);
    }

    public Long getDefaultStorageQuota() {
        String value = getConfigValue("default_storage_quota", "1073741824");
        return Long.parseLong(value);
    }

    public List<String> getAllowedFileExtensions() {
        String value = getConfigValue("allowed_file_extensions", "pdf,doc,docx,xls,xlsx,ppt,pptx,jpg,jpeg,png,gif,bmp,webp,txt,md,zip,rar");
        return List.of(value.split(","));
    }
}