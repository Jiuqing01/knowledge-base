package edu.ngd.controller;

import edu.ngd.dto.request.AddTagRequest;
import edu.ngd.dto.request.UpdateFileRequest;
import edu.ngd.dto.response.ApiResponse;
import edu.ngd.dto.response.FileResponse;
import edu.ngd.dto.response.TagResponse;
import edu.ngd.entity.File;
import edu.ngd.entity.Tag;
import edu.ngd.service.FileService;
import edu.ngd.service.JwtService;
import edu.ngd.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FileController {

    private final FileService fileService;
    private final JwtService jwtService;
    private final TagService tagService;

    private Long getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("用户未登录");
        }
        String token = authHeader.substring(7);
        return jwtService.getUserIdFromToken(token);
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long folderId,
            HttpServletRequest request) throws IOException {
        Long ownerId = getCurrentUserId(request);
        File uploadedFile = fileService.uploadFile(ownerId, file, folderId);
        return ResponseEntity.ok(ApiResponse.success("文件上传成功", FileResponse.fromFile(uploadedFile)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FileResponse>> updateFile(
            @PathVariable Long id,
            @RequestBody UpdateFileRequest request,
            HttpServletRequest httpRequest) {
        Long ownerId = getCurrentUserId(httpRequest);
        File file = fileService.updateFile(id, ownerId, request.getName(), request.getFolderId());
        return ResponseEntity.ok(ApiResponse.success("文件更新成功", FileResponse.fromFile(file)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable Long id, HttpServletRequest request) {
        Long ownerId = getCurrentUserId(request);
        fileService.deleteFile(id, ownerId);
        return ResponseEntity.ok(ApiResponse.success("文件删除成功"));
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<Void>> restoreFile(@PathVariable Long id, HttpServletRequest request) {
        Long ownerId = getCurrentUserId(request);
        fileService.restoreFile(id, ownerId);
        return ResponseEntity.ok(ApiResponse.success("文件恢复成功"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FileResponse>> getFile(@PathVariable Long id, HttpServletRequest request) {
        Long ownerId = getCurrentUserId(request);
        File file = fileService.getFile(id, ownerId);
        List<TagResponse> tags = tagService.getTagsForFile(id).stream()
                .map(TagResponse::fromTag)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(FileResponse.fromFile(file, tags)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FileResponse>>> getUserFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        Long ownerId = getCurrentUserId(request);
        List<File> files;
        if (keyword != null && !keyword.isEmpty()) {
            files = fileService.searchFiles(ownerId, keyword);
        } else {
            files = fileService.getUserFiles(ownerId);
        }
        List<FileResponse> responses = files.stream()
                .map(f -> {
                    List<TagResponse> tags = tagService.getTagsForFile(f.getId()).stream()
                            .map(TagResponse::fromTag)
                            .collect(Collectors.toList());
                    return FileResponse.fromFile(f, tags);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/folder/{folderId}")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getFilesInFolder(
            @PathVariable Long folderId,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        Long ownerId = getCurrentUserId(request);
        List<File> files = fileService.getFilesInFolder(folderId, ownerId);
        
        if (keyword != null && !keyword.isEmpty()) {
            files = files.stream()
                    .filter(f -> f.getOriginalName().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
        }
        List<FileResponse> responses = files.stream()
                .map(f -> {
                    List<TagResponse> tags = tagService.getTagsForFile(f.getId()).stream()
                            .map(TagResponse::fromTag)
                            .collect(Collectors.toList());
                    return FileResponse.fromFile(f, tags);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/shared")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getSharedFiles(HttpServletRequest request) {
        Long ownerId = getCurrentUserId(request);
        List<File> files = fileService.getUserFiles(ownerId);
        List<FileResponse> responses = files.stream()
                .map(f -> {
                    List<TagResponse> tags = tagService.getTagsForFile(f.getId()).stream()
                            .map(TagResponse::fromTag)
                            .collect(Collectors.toList());
                    return FileResponse.fromFile(f, tags);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/deleted")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getDeletedFiles(HttpServletRequest request) {
        Long ownerId = getCurrentUserId(request);
        List<File> files = fileService.getDeletedFiles(ownerId);
        List<FileResponse> responses = files.stream()
                .map(f -> {
                    List<TagResponse> tags = tagService.getTagsForFile(f.getId()).stream()
                            .map(TagResponse::fromTag)
                            .collect(Collectors.toList());
                    return FileResponse.fromFile(f, tags);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<FileResponse>>> searchFiles(@RequestParam String keyword, HttpServletRequest request) {
        Long ownerId = getCurrentUserId(request);
        List<File> files = fileService.searchFiles(ownerId, keyword);
        List<FileResponse> responses = files.stream()
                .map(f -> {
                    List<TagResponse> tags = tagService.getTagsForFile(f.getId()).stream()
                            .map(TagResponse::fromTag)
                            .collect(Collectors.toList());
                    return FileResponse.fromFile(f, tags);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/tag/{tagId}")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getFilesByTag(@PathVariable Long tagId, HttpServletRequest request) {
        Long ownerId = getCurrentUserId(request);
        List<File> files = fileService.getFilesByTag(tagId, ownerId);
        List<FileResponse> responses = files.stream()
                .map(f -> {
                    List<TagResponse> tags = tagService.getTagsForFile(f.getId()).stream()
                            .map(TagResponse::fromTag)
                            .collect(Collectors.toList());
                    return FileResponse.fromFile(f, tags);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}/rename")
    public ResponseEntity<ApiResponse<FileResponse>> renameFile(
            @PathVariable Long id,
            @RequestBody UpdateFileRequest request,
            HttpServletRequest httpRequest) {
        Long ownerId = getCurrentUserId(httpRequest);
        File file = fileService.updateFile(id, ownerId, request.getName(), null);
        return ResponseEntity.ok(ApiResponse.success("文件重命名成功", FileResponse.fromFile(file)));
    }

    @PostMapping("/{fileId}/tags")
    public ResponseEntity<ApiResponse<Void>> addTagToFile(
            @PathVariable Long fileId,
            @RequestBody AddTagRequest request,
            HttpServletRequest httpRequest) {
        Long ownerId = getCurrentUserId(httpRequest);
        tagService.addTagToFile(fileId, request.getTagId(), ownerId);
        return ResponseEntity.ok(ApiResponse.success("标签添加成功"));
    }

    @DeleteMapping("/{fileId}/tags/{tagId}")
    public ResponseEntity<ApiResponse<Void>> removeTagFromFile(
            @PathVariable Long fileId,
            @PathVariable Long tagId,
            HttpServletRequest httpRequest) {
        Long ownerId = getCurrentUserId(httpRequest);
        tagService.removeTagFromFile(fileId, tagId);
        return ResponseEntity.ok(ApiResponse.success("标签移除成功"));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id, HttpServletRequest request) {
        Long ownerId = getCurrentUserId(request);
        log.info("downloadFile called with id={}, ownerId={}", id, ownerId);
        
        File file = fileService.getFile(id, ownerId);
        if (file == null) {
            throw new RuntimeException("文件不存在");
        }
        
        String storagePath = file.getStoragePath();
        log.info("File storage path: {}", storagePath);
        
        if (storagePath == null || storagePath.isEmpty()) {
            throw new RuntimeException("文件存储路径为空");
        }
        
        Path filePath = Paths.get(storagePath);
        log.info("Resolved file path: {}", filePath.toAbsolutePath());
        
        Resource resource = new FileSystemResource(filePath);
        log.info("Resource exists: {}", resource.exists());

        if (!resource.exists()) {
            throw new RuntimeException("文件已被删除或不存在");
        }

        String filename;
        try {
            filename = URLEncoder.encode(file.getOriginalName(), "UTF-8")
                    .replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            filename = file.getOriginalName();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + filename)
                .body(resource);
    }

    @PostMapping("/download/batch")
    public ResponseEntity<Resource> downloadFiles(@RequestBody List<Long> fileIds, HttpServletRequest request) throws IOException {
        Long ownerId = getCurrentUserId(request);
        byte[] zipBytes = fileService.downloadFilesAsZip(fileIds, ownerId);
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "files_" + timestamp + ".zip";
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(new ByteArrayResource(zipBytes));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getAllFiles() {
        List<File> files = fileService.getAllFiles();
        List<FileResponse> responses = files.stream()
                .map(FileResponse::fromFile)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/admin/user/{userId}")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getFilesByUser(@PathVariable Long userId) {
        List<File> files = fileService.getUserFiles(userId);
        List<FileResponse> responses = files.stream()
                .map(FileResponse::fromFile)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/admin/download/{id}")
    public ResponseEntity<Resource> adminDownloadFile(@PathVariable Long id) {
        Path filePath = fileService.getFilePathForAdmin(id);
        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists()) {
            throw new RuntimeException("文件不存在");
        }

        String filename = filePath.getFileName().toString();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
}