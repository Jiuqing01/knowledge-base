package edu.ngd.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_tags")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Column(name = "tag_id", nullable = false)
    private Long tagId;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
}