package edu.ngd.controller;

import edu.ngd.dto.request.FolderRequest;
import edu.ngd.dto.response.ApiResponse;
import edu.ngd.dto.response.FolderResponse;
import edu.ngd.entity.Folder;
import edu.ngd.service.FolderService;
import edu.ngd.service.JwtService;
import edu.ngd.service.OperationLogService;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;
    private final JwtService jwtService;
    private final OperationLogService operationLogService;

    private Long getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("用户未登录");
        }
        String token = authHeader.substring(7);
        return jwtService.getUserIdFromToken(token);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FolderResponse>> createFolder(
            @Valid @RequestBody FolderRequest request,
            HttpServletRequest httpRequest) {
        Long ownerId = getCurrentUserId(httpRequest);
        Folder folder = folderService.createFolder(ownerId, request.getName(), request.getParentId());
        
        operationLogService.log(ownerId, "创建文件夹", "folder", folder.getId(), 
                "创建文件夹: " + request.getName());
        
        return ResponseEntity.ok(ApiResponse.success("文件夹创建成功", FolderResponse.fromFolder(folder)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FolderResponse>> updateFolder(
            @PathVariable Long id,
            @Valid @RequestBody FolderRequest request,
            HttpServletRequest httpRequest) {
        Long ownerId = getCurrentUserId(httpRequest);
        Folder folder = folderService.getFolder(id, ownerId);
        String oldName = folder.getName();
        folder = folderService.updateFolder(id, ownerId, request.getName());
        
        operationLogService.log(ownerId, "重命名文件夹", "folder", id, 
                "文件夹重命名: " + oldName + " -> " + request.getName());
        
        return ResponseEntity.ok(ApiResponse.success("文件夹更新成功", FolderResponse.fromFolder(folder)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFolder(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long ownerId = getCurrentUserId(httpRequest);
        Folder folder = folderService.getFolder(id, ownerId);
        String folderName = folder.getName();
        folderService.deleteFolder(id, ownerId);
        
        operationLogService.log(ownerId, "删除文件夹", "folder", id, "删除文件夹: " + folderName);
        
        return ResponseEntity.ok(ApiResponse.success("文件夹删除成功"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FolderResponse>> getFolder(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long ownerId = getCurrentUserId(httpRequest);
        Folder folder = folderService.getFolder(id, ownerId);
        return ResponseEntity.ok(ApiResponse.success(FolderResponse.fromFolder(folder)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FolderResponse>>> getUserFolders(HttpServletRequest httpRequest) {
        Long ownerId = getCurrentUserId(httpRequest);
        List<Folder> folders = folderService.getUserFolders(ownerId);
        List<FolderResponse> responses = folders.stream()
                .map(FolderResponse::fromFolder)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/root")
    public ResponseEntity<ApiResponse<List<FolderResponse>>> getRootFolders(HttpServletRequest httpRequest) {
        Long ownerId = getCurrentUserId(httpRequest);
        List<Folder> folders = folderService.getRootFolders(ownerId);
        List<FolderResponse> responses = folders.stream()
                .map(FolderResponse::fromFolder)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/children/{parentId}")
    public ResponseEntity<ApiResponse<List<FolderResponse>>> getChildFolders(
            @PathVariable Long parentId,
            HttpServletRequest httpRequest) {
        Long ownerId = getCurrentUserId(httpRequest);
        List<Folder> folders = folderService.getChildFolders(parentId, ownerId);
        List<FolderResponse> responses = folders.stream()
                .map(FolderResponse::fromFolder)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<FolderResponse>>> getFolderTree(HttpServletRequest httpRequest) {
        Long ownerId = getCurrentUserId(httpRequest);
        List<Folder> folders = folderService.getFolderTree(ownerId);
        List<FolderResponse> responses = folders.stream()
                .map(FolderResponse::fromFolder)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}