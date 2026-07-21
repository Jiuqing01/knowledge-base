package edu.ngd.entity;

import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "storage_name", nullable = false, unique = true, length = 64)
    private String storageName;

    @Column(name = "storage_path", nullable = false, length = 512)
    private String storagePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize = 0L;

    @Column(name = "mime_type", length = 128)
    private String mimeType;

    @Column(name = "file_hash", length = 64)
    private String fileHash;

    @Column(name = "folder_id", nullable = false)
    private Long folderId;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "is_deleted")
    private Integer isDeleted = 0;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public File() {}

    public File(Long id, String originalName, String storageName, String storagePath, 
                Long fileSize, String mimeType, String fileHash, Long folderId, 
                Long ownerId, Integer isDeleted, LocalDateTime deletedAt, 
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.originalName = originalName;
        this.storageName = storageName;
        this.storagePath = storagePath;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.fileHash = fileHash;
        this.folderId = folderId;
        this.ownerId = ownerId;
        this.isDeleted = isDeleted;
        this.deletedAt = deletedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public String getStorageName() { return storageName; }
    public void setStorageName(String storageName) { this.storageName = storageName; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getFileHash() { return fileHash; }
    public void setFileHash(String fileHash) { this.fileHash = fileHash; }

    public Long getFolderId() { return folderId; }
    public void setFolderId(Long folderId) { this.folderId = folderId; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String originalName;
        private String storageName;
        private String storagePath;
        private Long fileSize = 0L;
        private String mimeType;
        private String fileHash;
        private Long folderId;
        private Long ownerId;
        private Integer isDeleted = 0;
        private LocalDateTime deletedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder originalName(String originalName) { this.originalName = originalName; return this; }
        public Builder storageName(String storageName) { this.storageName = storageName; return this; }
        public Builder storagePath(String storagePath) { this.storagePath = storagePath; return this; }
        public Builder fileSize(Long fileSize) { this.fileSize = fileSize; return this; }
        public Builder mimeType(String mimeType) { this.mimeType = mimeType; return this; }
        public Builder fileHash(String fileHash) { this.fileHash = fileHash; return this; }
        public Builder folderId(Long folderId) { this.folderId = folderId; return this; }
        public Builder ownerId(Long ownerId) { this.ownerId = ownerId; return this; }
        public Builder isDeleted(Integer isDeleted) { this.isDeleted = isDeleted; return this; }
        public Builder deletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; return this; }

        public File build() {
            return new File(id, originalName, storageName, storagePath, fileSize, mimeType, fileHash, 
                           folderId, ownerId, isDeleted, deletedAt, null, null);
        }
    }
}