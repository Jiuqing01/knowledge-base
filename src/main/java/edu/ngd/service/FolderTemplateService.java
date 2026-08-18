package edu.ngd.service;

import edu.ngd.entity.Folder;
import edu.ngd.entity.FolderTemplate;
import edu.ngd.repository.FolderRepository;
import edu.ngd.repository.FolderTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * 将文本格式的模板（形如 "文档/工作/报告\n文档/个人/笔记"）解析并写入 folder_templates 表
     * 会先清空表，再按路径重建
     */
    @Transactional
    public void saveTemplatesFromText(String templatesText) {
        // 1. 清空现有模板
        folderTemplateRepository.deleteAllInBatch();
        folderTemplateRepository.flush();
        log.info("Cleared all folder templates");

        if (templatesText == null || templatesText.trim().isEmpty()) {
            return;
        }

        // 2. 解析每行路径，按顺序排序，同层去重
        List<List<String>> paths = Arrays.stream(templatesText.split("\\r?\\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .map(line -> Arrays.stream(line.split("/"))
                        .map(String::trim)
                        .filter(part -> !part.isEmpty())
                        .collect(Collectors.toList()))
                .filter(parts -> !parts.isEmpty())
                .collect(Collectors.toList());

        // 3. 按路径深度和出现顺序逐行插入，记录 (parentId, name) -> FolderTemplate
        java.util.Map<String, FolderTemplate> createdMap = new java.util.HashMap<>();
        java.util.Map<Long, Integer> parentSortOrder = new java.util.HashMap<>();

        for (List<String> parts : paths) {
            Long parentId = null;
            String parentKey = "";
            for (String part : parts) {
                String key = parentKey + "/" + part;
                if (!createdMap.containsKey(key)) {
                    int sortOrder = parentSortOrder.getOrDefault(parentId, 0);
                    parentSortOrder.put(parentId, sortOrder + 1);
                    FolderTemplate tpl = FolderTemplate.builder()
                            .name(part)
                            .parentId(parentId)
                            .sortOrder(sortOrder)
                            .build();
                    tpl = folderTemplateRepository.save(tpl);
                    createdMap.put(key, tpl);
                    log.info("Created folder template: path={}, id={}, parentId={}", key, tpl.getId(), parentId);
                }
                FolderTemplate current = createdMap.get(key);
                parentId = current.getId();
                parentKey = key;
            }
        }
        log.info("Saved folder templates from text, {} lines parsed, {} nodes created", 
                paths.size(), createdMap.size());
    }

    /**
     * 读取 folder_templates 表，拼接成文本格式（每行一个完整路径，按 sortOrder 排序）
     */
    public String getTemplatesAsText() {
        List<FolderTemplate> roots = folderTemplateRepository.findByParentIdIsNullOrderBySortOrder();
        List<String> lines = new ArrayList<>();
        for (FolderTemplate root : roots) {
            collectPaths(root, "", lines);
        }
        // 没有模板时返回默认值
        if (lines.isEmpty()) {
            return "文档/工作/报告\n文档/个人/笔记\n资源/图片\n资源/模板";
        }
        return String.join("\n", lines);
    }

    private void collectPaths(FolderTemplate node, String prefix, List<String> lines) {
        String current = prefix.isEmpty() ? node.getName() : prefix + "/" + node.getName();
        List<FolderTemplate> children = folderTemplateRepository.findByParentIdOrderBySortOrder(node.getId());
        if (children.isEmpty()) {
            lines.add(current);
        } else {
            // 若有子节点，父节点也保留为一条（避免只取叶子导致中间节点丢失）
            lines.add(current);
            for (FolderTemplate child : children) {
                collectPaths(child, current, lines);
            }
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