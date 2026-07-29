package edu.ngd.service;

import edu.ngd.entity.Folder;
import edu.ngd.entity.FolderTemplate;
import edu.ngd.repository.FolderRepository;
import edu.ngd.repository.FolderTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderTemplateService {

    private final FolderTemplateRepository folderTemplateRepository;
    private final FolderRepository folderRepository;

    @Transactional
    public void createFoldersForUser(Long userId) {
        List<FolderTemplate> rootTemplates = folderTemplateRepository.findByParentIdIsNullOrderBySortOrder();
        log.info("createFoldersForUser called for userId={}, found {} root templates", userId, rootTemplates.size());
        
        for (FolderTemplate template : rootTemplates) {
            log.info("Processing root template: id={}, name={}", template.getId(), template.getName());
            createFolderFromTemplate(template, userId, null);
        }
        
        List<Folder> createdFolders = folderRepository.findByOwnerIdAndIsDeleted(userId, 0);
        log.info("Created {} folders for user {}: {}", createdFolders.size(), userId, 
                createdFolders.stream().map(f -> f.getName()).collect(java.util.stream.Collectors.toList()));
    }

    private void createFolderFromTemplate(FolderTemplate template, Long userId, Long parentFolderId) {
        Folder folder = Folder.builder()
                .name(template.getName())
                .parentId(parentFolderId)
                .ownerId(userId)
                .isDeleted(0)
                .build();
        
        Folder savedFolder = folderRepository.save(folder);
        
        List<FolderTemplate> childTemplates = folderTemplateRepository.findByParentIdOrderBySortOrder(template.getId());
        for (FolderTemplate childTemplate : childTemplates) {
            createFolderFromTemplate(childTemplate, userId, savedFolder.getId());
        }
    }

    @Transactional
    public FolderTemplate createTemplate(String name, Long parentId) {
        FolderTemplate template = FolderTemplate.builder()
                .name(name)
                .parentId(parentId)
                .sortOrder(0)
                .build();
        return folderTemplateRepository.save(template);
    }

    public List<FolderTemplate> getAllTemplates() {
        return folderTemplateRepository.findAll();
    }

    @Transactional
    public void deleteTemplate(Long id) {
        folderTemplateRepository.deleteById(id);
    }
}