package edu.ngd.repository;

import edu.ngd.entity.FileTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileTagRepository extends JpaRepository<FileTag, Long> {

    List<FileTag> findByFileId(Long fileId);

    List<FileTag> findByTagId(Long tagId);

    Optional<FileTag> findByFileIdAndTagId(Long fileId, Long tagId);

    void deleteByFileId(Long fileId);

    void deleteByTagId(Long tagId);
}