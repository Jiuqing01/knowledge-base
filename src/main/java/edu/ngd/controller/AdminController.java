package edu.ngd.controller;

import edu.ngd.dto.response.ApiResponse;
import edu.ngd.entity.SystemConfig;
import edu.ngd.entity.User;
import edu.ngd.entity.UserRole;
import edu.ngd.repository.FileRepository;
import edu.ngd.repository.FolderRepository;
import edu.ngd.repository.UserRepository;
import edu.ngd.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final SystemConfigService systemConfigService;
    private final BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<User>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> users;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userRepository.findByUsernameContainingAndIsDeleted(keyword.trim(), 0, pageable);
        } else {
            users = userRepository.findByIsDeleted(0, pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        String password = (String) request.get("password");
        String role = (String) request.get("role");
        
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role != null ? UserRole.valueOf(role) : UserRole.USER);
        user.setStorageUsed(0L);
        user.setStorageQuota(systemConfigService.getDefaultStorageQuota());
        
        User saved = userRepository.save(user);
        log.info("Admin created user: id={}, username={}", saved.getId(), saved.getUsername());
        return ResponseEntity.ok(ApiResponse.success("用户创建成功", saved));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (request.containsKey("username")) {
            String username = (String) request.get("username");
            if (!username.equals(user.getUsername()) && 
                userRepository.findByUsername(username).isPresent()) {
                throw new RuntimeException("用户名已存在");
            }
            user.setUsername(username);
        }
        
        if (request.containsKey("role")) {
            user.setRole(UserRole.valueOf((String) request.get("role")));
        }
        
        
        
        User updated = userRepository.save(user);
        log.info("Admin updated user: id={}", id);
        return ResponseEntity.ok(ApiResponse.success("用户信息更新成功", updated));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if ("ADMIN".equals(user.getRole())) {
            throw new RuntimeException("不能删除管理员账户");
        }
        
        user.setIsDeleted(1);
        userRepository.save(user);
        log.info("Admin soft deleted user: id={}", id);
        return ResponseEntity.ok(ApiResponse.success("用户删除成功"));
    }

    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setPassword(passwordEncoder.encode("123456"));
        userRepository.save(user);
        log.info("Admin reset password for user: id={}", id);
        
        return ResponseEntity.ok(ApiResponse.success("密码已重置为 123456"));
    }

    @PutMapping("/users/{id}/quota")
    public ResponseEntity<ApiResponse<User>> updateQuota(
            @PathVariable Long id,
            @RequestBody Map<String, Long> request) {
        
        Long quota = request.get("quota");
        if (quota == null || quota <= 0) {
            throw new RuntimeException("配额值必须大于0");
        }
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setStorageQuota(quota);
        User updated = userRepository.save(user);
        log.info("Admin updated quota for user: id={}, quota={}", id, quota);
        
        return ResponseEntity.ok(ApiResponse.success("配额更新成功", updated));
    }

    @GetMapping("/config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminConfig() {
        Map<String, Object> config = new HashMap<>();
        
        SystemConfig fileExtensions = systemConfigService.getConfig("allowed_file_extensions");
        SystemConfig maxFileSize = systemConfigService.getConfig("max_file_size");
        SystemConfig defaultQuota = systemConfigService.getConfig("default_storage_quota");
        
        config.put("allowed_file_extensions", fileExtensions);
        config.put("max_file_size", maxFileSize);
        config.put("default_storage_quota", defaultQuota);
        
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    @PutMapping("/config")
    public ResponseEntity<ApiResponse<Void>> updateAdminConfig(@RequestBody Map<String, String> request) {
        if (request.containsKey("allowed_file_extensions")) {
            systemConfigService.updateConfig(
                    "allowed_file_extensions", 
                    request.get("allowed_file_extensions"),
                    "允许上传的文件扩展名白名单，逗号分隔，不区分大小写"
            );
        }
        
        if (request.containsKey("max_file_size")) {
            systemConfigService.updateConfig(
                    "max_file_size", 
                    request.get("max_file_size"),
                    "最大上传文件大小（字节）"
            );
        }
        
        if (request.containsKey("default_storage_quota")) {
            systemConfigService.updateConfig(
                    "default_storage_quota", 
                    request.get("default_storage_quota"),
                    "新用户默认存储配额（字节）"
            );
        }
        
        log.info("Admin updated system config");
        return ResponseEntity.ok(ApiResponse.success("配置更新成功"));
    }

    @GetMapping("/folder-templates")
    public ResponseEntity<ApiResponse<SystemConfig>> getFolderTemplates() {
        SystemConfig templates = systemConfigService.getConfig("folder_templates");
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    @PutMapping("/folder-templates")
    public ResponseEntity<ApiResponse<SystemConfig>> updateFolderTemplates(
            @RequestBody Map<String, String> request) {
        
        String templates = request.get("templates");
        if (templates == null || templates.trim().isEmpty()) {
            throw new RuntimeException("模板内容不能为空");
        }
        
        SystemConfig config = systemConfigService.updateConfig(
                "folder_templates",
                templates,
                "新用户初始文件夹模板，支持树形结构，格式如：文档/工作/报告,文档/个人/笔记"
        );
        
        log.info("Admin updated folder templates");
        return ResponseEntity.ok(ApiResponse.success("文件夹模板更新成功", config));
    }

    @GetMapping("/files")
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> getAllFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<edu.ngd.entity.File> files;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            files = fileRepository.searchByOriginalNameContainingAndIsDeleted(keyword.trim(), 0, pageable);
        } else {
            files = fileRepository.findByIsDeleted(0, pageable);
        }
        
        Page<Map<String, Object>> result = files.map(file -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", file.getId());
            map.put("originalName", file.getOriginalName());
            map.put("storageName", file.getStorageName());
            map.put("storagePath", file.getStoragePath());
            map.put("fileSize", file.getFileSize());
            map.put("mimeType", file.getMimeType());
            map.put("fileHash", file.getFileHash());
            map.put("folderId", file.getFolderId());
            map.put("ownerId", file.getOwnerId());
            map.put("isDeleted", file.getIsDeleted());
            map.put("createdAt", file.getCreatedAt());
            map.put("updatedAt", file.getUpdatedAt());
            
            User owner = userRepository.findById(file.getOwnerId()).orElse(null);
            map.put("ownerUsername", owner != null ? owner.getUsername() : "未知");
            
            String folderName = "";
            if (file.getFolderId() != null) {
                edu.ngd.entity.Folder folder = folderRepository.findById(file.getFolderId()).orElse(null);
                folderName = folder != null ? folder.getName() : "";
            }
            map.put("folderName", folderName);
            
            return map;
        });
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/files/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable Long id) {
        edu.ngd.entity.File file = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文件不存在"));
        
        file.setIsDeleted(1);
        fileRepository.save(file);
        log.info("Admin soft deleted file: id={}", id);
        return ResponseEntity.ok(ApiResponse.success("文件删除成功"));
    }
}
