package edu.ngd.controller;

import edu.ngd.dto.response.ApiResponse;
import edu.ngd.entity.FolderTemplate;
import edu.ngd.repository.FolderTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/folder-templates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FolderTemplateController {

    private final FolderTemplateRepository folderTemplateRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTemplates() {
        List<FolderTemplate> rootTemplates = folderTemplateRepository.findByParentIdIsNullOrderBySortOrder();
        List<Map<String, Object>> tree = buildTree(rootTemplates);
        return ResponseEntity.ok(ApiResponse.success(tree));
    }

    private List<Map<String, Object>> buildTree(List<FolderTemplate> templates) {
        List<Map<String, Object>> tree = new ArrayList<>();
        
        for (FolderTemplate template : templates) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", template.getId());
            node.put("label", template.getName());
            node.put("parentId", template.getParentId());
            node.put("sortOrder", template.getSortOrder());
            node.put("children", buildTree(folderTemplateRepository.findByParentIdOrderBySortOrder(template.getId())));
            tree.add(node);
        }
        
        return tree;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FolderTemplate>> createTemplate(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        Long parentId = request.get("parentId") != null ? ((Number) request.get("parentId")).longValue() : null;
        
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("模板名称不能为空");
        }
        
        FolderTemplate template = FolderTemplate.builder()
                .name(name.trim())
                .parentId(parentId == null || parentId == 0 ? null : parentId)
                .build();
        
        FolderTemplate saved = folderTemplateRepository.save(template);
        log.info("Created folder template: id={}, name={}, parentId={}", saved.getId(), saved.getName(), saved.getParentId());
        return ResponseEntity.ok(ApiResponse.success("模板创建成功", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FolderTemplate>> updateTemplate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        
        FolderTemplate template = folderTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("模板不存在"));
        
        if (request.containsKey("name")) {
            String name = (String) request.get("name");
            if (name == null || name.trim().isEmpty()) {
                throw new RuntimeException("模板名称不能为空");
            }
            template.setName(name.trim());
        }
        
        if (request.containsKey("parentId")) {
            Long parentId = request.get("parentId") != null ? ((Number) request.get("parentId")).longValue() : null;
            template.setParentId(parentId == null || parentId == 0 ? null : parentId);
        }
        
        FolderTemplate updated = folderTemplateRepository.save(template);
        log.info("Updated folder template: id={}, name={}", updated.getId(), updated.getName());
        return ResponseEntity.ok(ApiResponse.success("模板更新成功", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable Long id) {
        FolderTemplate template = folderTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("模板不存在"));
        
        List<FolderTemplate> children = folderTemplateRepository.findByParentIdOrderBySortOrder(id);
        if (!children.isEmpty()) {
            throw new RuntimeException("请先删除子模板");
        }
        
        folderTemplateRepository.delete(template);
        log.info("Deleted folder template: id={}, name={}", id, template.getName());
        return ResponseEntity.ok(ApiResponse.success("模板删除成功"));
    }

    @GetMapping("/flat")
    public ResponseEntity<ApiResponse<List<FolderTemplate>>> getFlatTemplates() {
        List<FolderTemplate> allTemplates = folderTemplateRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(allTemplates));
    }
}
