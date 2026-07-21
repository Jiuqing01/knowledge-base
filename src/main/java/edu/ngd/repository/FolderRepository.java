package edu.ngd.repository;

import edu.ngd.entity.Folder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findByOwnerIdAndParentIdAndIsDeleted(Long ownerId, Long parentId, Integer isDeleted);
    List<Folder> findByOwnerIdAndIsDeleted(Long ownerId, Integer isDeleted);
    Page<Folder> findByParentIdAndIsDeleted(Long parentId, Integer isDeleted, Pageable pageable);
    boolean existsByParentId(Long parentId);
    List<Folder> findByParentIdAndIsDeleted(Long parentId, Integer isDeleted);
    List<Folder> findByOwnerIdAndParentIdIsNullAndIsDeleted(Long ownerId, Integer isDeleted);
    List<Folder> findByParentIdAndOwnerIdAndIsDeleted(Long parentId, Long ownerId, Integer isDeleted);
    Optional<Folder> findByIdAndOwnerIdAndIsDeleted(Long id, Long ownerId, Integer isDeleted);
    Optional<Folder> findByIdAndIsDeleted(Long id, Integer isDeleted);
}