package edu.ngd.service;

import edu.ngd.entity.Folder;
import edu.ngd.repository.FileRepository;
import edu.ngd.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;

    @Transactional
    public Folder createFolder(Long ownerId, String name, Long parentId) {
        if (parentId != null && !folderRepository.findByIdAndIsDeleted(parentId, 0).isPresent()) {
            throw new RuntimeException("父文件夹不存在");
        }

        Folder folder = Folder.builder()
                .name(name)
                .parentId(parentId)
                .ownerId(ownerId)
                .isDeleted(0)
                .build();

        Folder savedFolder = folderRepository.save(folder);
        log.info("Created folder: id={}, name={}, ownerId={}", savedFolder.getId(), savedFolder.getName(), ownerId);
        return savedFolder;
    }

    @Transactional
    public Folder updateFolder(Long id, Long ownerId, String name) {
        Folder folder = folderRepository.findByIdAndOwnerIdAndIsDeleted(id, ownerId, 0)
                .orElseThrow(() -> new RuntimeException("文件夹不存在"));

        folder.setName(name);
        Folder updatedFolder = folderRepository.save(folder);
        log.info("Updated folder: id={}, name={}", updatedFolder.getId(), updatedFolder.getName());
        return updatedFolder;
    }

    @Transactional
    public void deleteFolder(Long id, Long ownerId) {
        Folder folder = folderRepository.findByIdAndOwnerIdAndIsDeleted(id, ownerId, 0)
                .orElseThrow(() -> new RuntimeException("文件夹不存在"));

        long fileCount = fileRepository.countByFolderIdAndIsDeleted(id, 0);
        if (fileCount > 0) {
            throw new RuntimeException("文件夹不为空，请先清空或移动内容");
        }

        List<Folder> children = folderRepository.findByParentIdAndOwnerIdAndIsDeleted(id, ownerId, 0);
        for (Folder child : children) {
            deleteFolder(child.getId(), ownerId);
        }

        folder.setIsDeleted(1);
        folderRepository.save(folder);
        log.info("Deleted folder: id={}, name={}", id, folder.getName());
    }

    public Folder getFolder(Long id, Long ownerId) {
        return folderRepository.findByIdAndOwnerIdAndIsDeleted(id, ownerId, 0)
                .orElseThrow(() -> new RuntimeException("文件夹不存在"));
    }

    public List<Folder> getUserFolders(Long ownerId) {
        return folderRepository.findByOwnerIdAndIsDeleted(ownerId, 0);
    }

    public List<Folder> getRootFolders(Long ownerId) {
        return folderRepository.findByOwnerIdAndParentIdIsNullAndIsDeleted(ownerId, 0);
    }

    public List<Folder> getChildFolders(Long parentId, Long ownerId) {
        return folderRepository.findByParentIdAndOwnerIdAndIsDeleted(parentId, ownerId, 0);
    }

    public List<Folder> getFolderTree(Long ownerId) {
        List<Folder> allFolders = folderRepository.findByOwnerIdAndIsDeleted(ownerId, 0);
        return buildTree(allFolders, null);
    }

    private List<Folder> buildTree(List<Folder> folders, Long parentId) {
        return folders.stream()
                .filter(folder -> java.util.Objects.equals(folder.getParentId(), parentId))
                .peek(folder -> folder.setParentId(null))
                .collect(Collectors.toList());
    }
}