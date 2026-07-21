package edu.ngd.controller;

import edu.ngd.dto.request.TagRequest;
import edu.ngd.dto.response.ApiResponse;
import edu.ngd.dto.response.TagResponse;
import edu.ngd.entity.Tag;
import edu.ngd.service.JwtService;
import edu.ngd.service.TagService;
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
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;
    private final JwtService jwtService;

    private Long getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("用户未登录");
        }
        String token = authHeader.substring(7);
        return jwtService.getUserIdFromToken(token);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TagResponse>> createTag(
            @Valid @RequestBody TagRequest request,
            HttpServletRequest httpRequest) {
        Long ownerId = getCurrentUserId(httpRequest);
        Tag tag = tagService.createTag(ownerId, request.getName(), request.getColor());
        return ResponseEntity.ok(ApiResponse.success("标签创建成功", TagResponse.fromTag(tag)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TagResponse>> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody TagRequest request,
            HttpServletRequest httpRequest) {
        Long ownerId = getCurrentUserId(httpRequest);
        Tag tag = tagService.updateTag(id, ownerId, request.getName(), request.getColor());
        return ResponseEntity.ok(ApiResponse.success("标签更新成功", TagResponse.fromTag(tag)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long ownerId = getCurrentUserId(httpRequest);
        tagService.deleteTag(id, ownerId);
        return ResponseEntity.ok(ApiResponse.success("标签删除成功"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TagResponse>> getTag(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long ownerId = getCurrentUserId(httpRequest);
        Tag tag = tagService.getTag(id, ownerId);
        return ResponseEntity.ok(ApiResponse.success(TagResponse.fromTag(tag)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponse>>> getUserTags(HttpServletRequest httpRequest) {
        Long ownerId = getCurrentUserId(httpRequest);
        List<Tag> tags = tagService.getUserTags(ownerId);
        List<TagResponse> responses = tags.stream()
                .map(tag -> TagResponse.fromTag(tag, (long) tagService.getFilesWithTag(tag.getId()).size()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/{tagId}/files/{fileId}")
    public ResponseEntity<ApiResponse<Void>> addTagToFile(
            @PathVariable Long tagId,
            @PathVariable Long fileId,
            HttpServletRequest httpRequest) {
        Long ownerId = getCurrentUserId(httpRequest);
        tagService.addTagToFile(fileId, tagId, ownerId);
        return ResponseEntity.ok(ApiResponse.success("标签添加成功"));
    }

    @DeleteMapping("/{tagId}/files/{fileId}")
    public ResponseEntity<ApiResponse<Void>> removeTagFromFile(
            @PathVariable Long tagId,
            @PathVariable Long fileId) {
        tagService.removeTagFromFile(fileId, tagId);
        return ResponseEntity.ok(ApiResponse.success("标签移除成功"));
    }

    @GetMapping("/files/{fileId}")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getTagsForFile(@PathVariable Long fileId) {
        List<Tag> tags = tagService.getTagsForFile(fileId);
        List<TagResponse> responses = tags.stream()
                .map(TagResponse::fromTag)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{tagId}/files")
    public ResponseEntity<ApiResponse<List<Long>>> getFilesWithTag(@PathVariable Long tagId) {
        List<Long> fileIds = tagService.getFilesWithTag(tagId);
        return ResponseEntity.ok(ApiResponse.success(fileIds));
    }
}