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
        
        for (FolderTemplate template : rootTemplates) {
            createFolderFromTemplate(template, userId, null);
        }
        
        log.info("Created folders for user: {}", userId);
    }

    private void createFolderFromTemplate(FolderTemplate template, Long userId, Long parentFolderId) {
        Folder folder = Folder.builder()
                .name(template.getName())
                .parentId(parentFolderId)
                .ownerId(userId)
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