package edu.ngd.dto.response;

import edu.ngd.entity.Tag;

import java.time.LocalDateTime;

public class TagResponse {

    private Long id;
    private String name;
    private Long ownerId;
    private String color;
    private LocalDateTime createdAt;
    private Long fileCount;

    public TagResponse() {}

    public TagResponse(Long id, String name, Long ownerId, String color, LocalDateTime createdAt, Long fileCount) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.color = color;
        this.createdAt = createdAt;
        this.fileCount = fileCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getFileCount() { return fileCount; }
    public void setFileCount(Long fileCount) { this.fileCount = fileCount; }

    public static TagResponse fromTag(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getOwnerId(), tag.getColor(), tag.getCreatedAt(), null);
    }

    public static TagResponse fromTag(Tag tag, Long fileCount) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getOwnerId(), tag.getColor(), tag.getCreatedAt(), fileCount);
    }
}
