package edu.ngd.dto.response;

import edu.ngd.entity.File;

import java.time.LocalDateTime;
import java.util.List;

public class FileResponse {

    private Long id;
    private String originalName;
    private String storageName;
    private String storagePath;
    private Long fileSize;
    private String mimeType;
    private String fileHash;
    private Long folderId;
    private Long ownerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TagResponse> tags;

    public FileResponse() {
    }

    public FileResponse(Long id, String originalName, String storageName, String storagePath, 
                        Long fileSize, String mimeType, String fileHash, Long folderId, 
                        Long ownerId, LocalDateTime createdAt, LocalDateTime updatedAt, 
                        List<TagResponse> tags) {
        this.id = id;
        this.originalName = originalName;
        this.storageName = storageName;
        this.storagePath = storagePath;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.fileHash = fileHash;
        this.folderId = folderId;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.tags = tags;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getStorageName() {
        return storageName;
    }

    public void setStorageName(String storageName) {
        this.storageName = storageName;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public Long getFolderId() {
        return folderId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<TagResponse> getTags() {
        return tags;
    }

    public void setTags(List<TagResponse> tags) {
        this.tags = tags;
    }

    public static FileResponse fromFile(File file) {
        return new FileResponse(
                file.getId(),
                file.getOriginalName(),
                file.getStorageName(),
                file.getStoragePath(),
                file.getFileSize(),
                file.getMimeType(),
                file.getFileHash(),
                file.getFolderId(),
                file.getOwnerId(),
                file.getCreatedAt(),
                file.getUpdatedAt(),
                null
        );
    }

    public static FileResponse fromFile(File file, List<TagResponse> tags) {
        return new FileResponse(
                file.getId(),
                file.getOriginalName(),
                file.getStorageName(),
                file.getStoragePath(),
                file.getFileSize(),
                file.getMimeType(),
                file.getFileHash(),
                file.getFolderId(),
                file.getOwnerId(),
                file.getCreatedAt(),
                file.getUpdatedAt(),
                tags
        );
    }
}