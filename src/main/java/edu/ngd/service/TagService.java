package edu.ngd.service;

import edu.ngd.entity.FileTag;
import edu.ngd.entity.Tag;
import edu.ngd.repository.FileRepository;
import edu.ngd.repository.FileTagRepository;
import edu.ngd.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final FileTagRepository fileTagRepository;
    private final FileRepository fileRepository;

    @Transactional
    public Tag createTag(Long userId, String name, String color) {
        if (tagRepository.existsByNameAndOwnerId(name, userId)) {
            throw new RuntimeException("标签名称已存在");
        }

        Tag tag = Tag.builder()
                .name(name)
                .ownerId(userId)
                .color(color != null ? color : "#409eff")
                .build();

        Tag savedTag = tagRepository.save(tag);
        log.info("Created tag: id={}, name={}, userId={}", savedTag.getId(), name, userId);
        return savedTag;
    }

    @Transactional
    public Tag updateTag(Long id, Long userId, String name, String color) {
        Tag tag = tagRepository.findByIdAndOwnerId(id, userId)
                .orElseThrow(() -> new RuntimeException("标签不存在"));

        if (name != null && !name.isEmpty()) {
            if (!name.equals(tag.getName()) && tagRepository.existsByNameAndOwnerId(name, userId)) {
                throw new RuntimeException("标签名称已存在");
            }
            tag.setName(name);
        }
        if (color != null) {
            tag.setColor(color);
        }

        Tag updatedTag = tagRepository.save(tag);
        log.info("Updated tag: id={}, name={}", updatedTag.getId(), updatedTag.getName());
        return updatedTag;
    }

    @Transactional
    public void deleteTag(Long id, Long userId) {
        Tag tag = tagRepository.findByIdAndOwnerId(id, userId)
                .orElseThrow(() -> new RuntimeException("标签不存在"));

        fileTagRepository.deleteByTagId(id);
        tagRepository.delete(tag);

        log.info("Deleted tag: id={}, name={}", id, tag.getName());
    }

    public Tag getTag(Long id, Long userId) {
        return tagRepository.findByIdAndOwnerId(id, userId)
                .orElseThrow(() -> new RuntimeException("标签不存在"));
    }

    public List<Tag> getUserTags(Long userId) {
        return tagRepository.findByOwnerId(userId);
    }

    @Transactional
    public void addTagToFile(Long fileId, Long tagId, Long userId) {
        if (!fileRepository.existsById(fileId)) {
            throw new RuntimeException("文件不存在");
        }
        if (!tagRepository.existsById(tagId)) {
            throw new RuntimeException("标签不存在");
        }

        if (fileTagRepository.findByFileIdAndTagId(fileId, tagId).isPresent()) {
            throw new RuntimeException("文件已添加该标签");
        }

        FileTag fileTag = FileTag.builder()
                .fileId(fileId)
                .tagId(tagId)
                .build();

        fileTagRepository.save(fileTag);
        log.info("Added tag {} to file {}", tagId, fileId);
    }

    @Transactional
    public void removeTagFromFile(Long fileId, Long tagId) {
        FileTag fileTag = fileTagRepository.findByFileIdAndTagId(fileId, tagId)
                .orElseThrow(() -> new RuntimeException("文件未添加该标签"));

        fileTagRepository.delete(fileTag);
        log.info("Removed tag {} from file {}", tagId, fileId);
    }

    public List<Tag> getTagsForFile(Long fileId) {
        List<FileTag> fileTags = fileTagRepository.findByFileId(fileId);
        List<Tag> tags = new ArrayList<>();
        for (FileTag fileTag : fileTags) {
            tagRepository.findById(fileTag.getTagId())
                    .ifPresent(tags::add);
        }
        return tags;
    }

    public List<Long> getFilesWithTag(Long tagId) {
        List<FileTag> fileTags = fileTagRepository.findByTagId(tagId);
        List<Long> fileIds = new ArrayList<>();
        for (FileTag fileTag : fileTags) {
            fileRepository.findByIdAndIsDeleted(fileTag.getFileId(), 0)
                    .ifPresent(file -> fileIds.add(file.getId()));
        }
        return fileIds;
    }
}