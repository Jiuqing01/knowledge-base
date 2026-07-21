package edu.ngd.repository;

import edu.ngd.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByOwnerId(Long ownerId);

    Optional<Tag> findByIdAndOwnerId(Long id, Long ownerId);

    Optional<Tag> findByNameAndOwnerId(String name, Long ownerId);

    boolean existsByNameAndOwnerId(String name, Long ownerId);
}