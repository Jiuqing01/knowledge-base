package edu.ngd.service;

import edu.ngd.config.FileUploadConfig;
import edu.ngd.entity.File;
import edu.ngd.repository.FileRepository;
import edu.ngd.repository.FileTagRepository;
import edu.ngd.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final FileTagRepository fileTagRepository;
    private final FolderRepository folderRepository;
    private final UserService userService;
    private final FileUploadConfig fileUploadConfig;

    @Transactional
    public File uploadFile(Long ownerId, MultipartFile file, Long folderId) throws IOException {
        return uploadFile(ownerId, file, folderId, false);
    }

    @Transactional
    public File uploadFile(Long ownerId, MultipartFile file, Long folderId, boolean overwrite) throws IOException {
        String originalName = file.getOriginalFilename();
        String mimeType = file.getContentType();
        Long fileSize = file.getSize();

        validateFile(file, originalName, fileSize);

        if (!userService.hasEnoughStorage(ownerId, fileSize)) {
            throw new RuntimeException("存储空间不足");
        }

        java.util.Optional<File> existingFile = fileRepository.findByFolderIdAndOriginalNameAndIsDeleted(folderId, originalName, 0);
        if (existingFile.isPresent()) {
            if (!overwrite) {
                throw new RuntimeException("同名文件已存在");
            } else {
                File oldFile = existingFile.get();
                userService.updateStorageUsed(ownerId, -oldFile.getFileSize());
                
                java.nio.file.Path oldFilePath = Paths.get(oldFile.getStoragePath());
                if (Files.exists(oldFilePath)) {
                    Files.delete(oldFilePath);
                }
                
                oldFile.setIsDeleted(1);
                oldFile.setDeletedAt(LocalDateTime.now());
                fileRepository.save(oldFile);
            }
        }

        String extension = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".")).toLowerCase()
                : "";
        String storageName = UUID.randomUUID().toString() + extension;

        Path uploadPath = Paths.get(fileUploadConfig.getUploadDir()).toAbsolutePath().normalize()
                .resolve(String.valueOf(ownerId));
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path storagePath = uploadPath.resolve(storageName);
        file.transferTo(storagePath.toFile());

        String fileHash = calculateFileHash(storagePath);

        File fileEntity = File.builder()
                .originalName(originalName)
                .storageName(storageName)
                .storagePath(storagePath.toString())
                .mimeType(mimeType)
                .fileSize(fileSize)
                .fileHash(fileHash)
                .folderId(folderId)
                .ownerId(ownerId)
                .isDeleted(0)
                .build();

        File savedFile = fileRepository.save(fileEntity);

        userService.updateStorageUsed(ownerId, fileSize);

        log.info("Uploaded file: id={}, name={}, size={}, ownerId={}, overwrite={}",
                savedFile.getId(), originalName, fileSize, ownerId, overwrite);
        return savedFile;
    }

    private String calculateFileHash(Path filePath) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = Files.readAllBytes(filePath);
            byte[] hashBytes = digest.digest(fileBytes);
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            log.warn("Failed to calculate file hash", e);
            return null;
        }
    }

    @Transactional
    public File updateFile(Long id, Long ownerId, String name, Long folderId) {
        File file = fileRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new RuntimeException("文件不存在"));

        if (name != null && !name.isEmpty()) {
            file.setOriginalName(name);
        }
        if (folderId != null) {
            file.setFolderId(folderId);
        }

        File updatedFile = fileRepository.save(file);
        log.info("Updated file: id={}, name={}", updatedFile.getId(), updatedFile.getOriginalName());
        return updatedFile;
    }

    @Transactional
    public void deleteFile(Long id, Long ownerId) {
        File file = fileRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new RuntimeException("文件不存在"));

        file.setIsDeleted(1);
        file.setDeletedAt(LocalDateTime.now());
        fileRepository.save(file);

        userService.updateStorageUsed(ownerId, -file.getFileSize());

        log.info("Deleted file: id={}, name={}", id, file.getOriginalName());
    }

    @Transactional
    public void restoreFile(Long id, Long ownerId) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文件不存在"));

        if (file.getIsDeleted() != 1) {
            throw new RuntimeException("文件未被删除");
        }

        file.setIsDeleted(0);
        file.setDeletedAt(null);
        fileRepository.save(file);

        userService.updateStorageUsed(ownerId, file.getFileSize());

        log.info("Restored file: id={}, name={}", id, file.getOriginalName());
    }

    public File getFile(Long id, Long ownerId) {
        log.info("getFile called with id={}, ownerId={}", id, ownerId);
        java.util.Optional<File> fileOpt = fileRepository.findByIdAndOwnerIdAndIsDeleted(id, ownerId, 0);
        if (fileOpt.isPresent()) {
            File file = fileOpt.get();
            log.info("File found: id={}, name={}, storagePath={}", 
                    file.getId(), file.getOriginalName(), file.getStoragePath());
            
            if (file.getStoragePath() == null || file.getStoragePath().isEmpty()) {
                log.info("Storage path is null, trying to find file by size and extension");
                String storagePath = findFileBySizeAndExtension(file, ownerId);
                if (storagePath != null) {
                    file.setStoragePath(storagePath);
                    file.setStorageName(java.nio.file.Paths.get(storagePath).getFileName().toString());
                    fileRepository.save(file);
                    log.info("Updated file storage path: {}", storagePath);
                }
            }
            
            return file;
        } else {
            log.warn("File not found: id={}, ownerId={}", id, ownerId);
            throw new RuntimeException("文件不存在");
        }
    }
    
    private String findFileBySizeAndExtension(File file, Long ownerId) {
        try {
            java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads").resolve(String.valueOf(ownerId));
            if (!java.nio.file.Files.exists(uploadPath)) {
                log.warn("Upload path not found: {}", uploadPath);
                return null;
            }
            
            final String extension;
            if (file.getOriginalName() != null && file.getOriginalName().contains(".")) {
                extension = file.getOriginalName().substring(file.getOriginalName().lastIndexOf(".")).toLowerCase();
            } else {
                extension = "";
            }
            
            final Long fileSize = file.getFileSize();
            
            java.util.List<java.nio.file.Path> candidates = java.nio.file.Files.list(uploadPath)
                    .filter(p -> {
                        try {
                            String fileName = p.getFileName().toString().toLowerCase();
                            boolean matchesExtension = extension.isEmpty() || fileName.endsWith(extension);
                            boolean matchesSize = java.nio.file.Files.size(p) == fileSize;
                            return matchesExtension && matchesSize;
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            if (candidates.isEmpty()) {
                log.warn("No matching file found for id={}, size={}, ext={}", 
                        file.getId(), fileSize, extension);
                return null;
            }
            
            java.nio.file.Path matchedFile = candidates.get(0);
            log.info("Found matching file: {}", matchedFile);
            return matchedFile.toString();
        } catch (Exception e) {
            log.error("Error finding file by size and extension: {}", e.getMessage());
            return null;
        }
    }

    public List<File> getUserFiles(Long ownerId) {
        return fileRepository.findByOwnerIdAndIsDeleted(ownerId, 0);
    }

    public List<File> getFilesInFolder(Long folderId, Long ownerId) {
        List<Long> folderIds = getAllFolderIds(folderId, ownerId);
        return fileRepository.findByFolderIdInAndOwnerIdAndIsDeleted(folderIds, ownerId, 0);
    }

    private List<Long> getAllFolderIds(Long parentId, Long ownerId) {
        List<Long> folderIds = new java.util.ArrayList<>();
        folderIds.add(parentId);
        addChildFolderIds(parentId, ownerId, folderIds);
        return folderIds;
    }

    private void addChildFolderIds(Long parentId, Long ownerId, List<Long> folderIds) {
        List<edu.ngd.entity.Folder> childFolders = folderRepository.findByParentIdAndOwnerIdAndIsDeleted(parentId, ownerId, 0);
        for (edu.ngd.entity.Folder folder : childFolders) {
            folderIds.add(folder.getId());
            addChildFolderIds(folder.getId(), ownerId, folderIds);
        }
    }

    public List<File> getDeletedFiles(Long ownerId) {
        return fileRepository.findByOwnerIdAndIsDeleted(ownerId, 1);
    }

    public List<File> searchFiles(Long ownerId, String keyword) {
        return fileRepository.findByOwnerIdAndOriginalNameContainingAndIsDeleted(ownerId, keyword, 0);
    }

    private void validateFile(MultipartFile file, String originalName, Long fileSize) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        if (fileSize > fileUploadConfig.getMaxFileSize()) {
            throw new RuntimeException("文件大小超过限制");
        }

        if (originalName != null && originalName.contains(".")) {
            String extension = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
            if (!fileUploadConfig.getAllowedExtensions().contains(extension)) {
                throw new RuntimeException("不支持的文件类型");
            }
        }
    }

    public Path getFilePath(Long id, Long ownerId) {
        File file = getFile(id, ownerId);
        return Paths.get(file.getStoragePath());
    }

    public byte[] downloadFilesAsZip(List<Long> fileIds, Long ownerId) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos)) {
            for (Long fileId : fileIds) {
                File file = getFile(fileId, ownerId);
                Path storagePath = Paths.get(file.getStoragePath());
                
                if (Files.exists(storagePath)) {
                    ZipArchiveEntry entry = new ZipArchiveEntry(file.getOriginalName());
                    zos.putArchiveEntry(entry);
                    Files.copy(storagePath, zos);
                    zos.closeArchiveEntry();
                }
            }
        }
        log.info("Created zip file for {} files, ownerId={}", fileIds.size(), ownerId);
        return baos.toByteArray();
    }

    public List<File> getAllFiles() {
        return fileRepository.findAll().stream()
                .filter(f -> f.getIsDeleted() != 1)
                .collect(java.util.stream.Collectors.toList());
    }

    public Path getFilePathForAdmin(Long id) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文件不存在"));
        return Paths.get(file.getStoragePath());
    }

    public List<File> getFilesByTag(Long tagId, Long ownerId) {
        List<edu.ngd.entity.FileTag> fileTags = fileTagRepository.findByTagId(tagId);
        List<Long> fileIds = fileTags.stream()
                .map(edu.ngd.entity.FileTag::getFileId)
                .collect(java.util.stream.Collectors.toList());
        return fileRepository.findAllById(fileIds).stream()
                .filter(f -> f.getOwnerId().equals(ownerId) && f.getIsDeleted() != 1)
                .collect(java.util.stream.Collectors.toList());
    }
}