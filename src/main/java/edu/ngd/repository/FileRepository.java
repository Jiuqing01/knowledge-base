package edu.ngd.repository;

import edu.ngd.entity.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    List<File> findByOwnerIdAndIsDeleted(Long ownerId, Integer isDeleted);

    List<File> findByFolderIdAndIsDeleted(Long folderId, Integer isDeleted);

    List<File> findByFolderIdAndOwnerIdAndIsDeleted(Long folderId, Long ownerId, Integer isDeleted);

    List<File> findByFolderIdInAndOwnerIdAndIsDeleted(List<Long> folderIds, Long ownerId, Integer isDeleted);

    Optional<File> findByIdAndIsDeleted(Long id, Integer isDeleted);

    Optional<File> findByIdAndOwnerId(Long id, Long ownerId);
    
    Optional<File> findByIdAndOwnerIdAndIsDeleted(Long id, Long ownerId, Integer isDeleted);

    List<File> findByOwnerIdAndOriginalNameContainingAndIsDeleted(Long ownerId, String originalName, Integer isDeleted);

    Optional<File> findByFolderIdAndOriginalNameAndIsDeleted(Long folderId, String originalName, Integer isDeleted);

    long countByFolderIdAndIsDeleted(Long folderId, Integer isDeleted);

    Page<File> findByIsDeleted(Integer isDeleted, Pageable pageable);

    Page<File> searchByOriginalNameContainingAndIsDeleted(String originalName, Integer isDeleted, Pageable pageable);

    Page<File> findByOwnerIdAndIsDeleted(Long ownerId, Integer isDeleted, Pageable pageable);

    Page<File> findByOwnerIdAndOriginalNameContainingAndIsDeleted(Long ownerId, String originalName, Integer isDeleted, Pageable pageable);

    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM File f WHERE f.ownerId = :ownerId AND f.isDeleted = :isDeleted")
    Long sumFileSizeByOwnerIdAndIsDeleted(@Param("ownerId") Long ownerId, @Param("isDeleted") Integer isDeleted);
}