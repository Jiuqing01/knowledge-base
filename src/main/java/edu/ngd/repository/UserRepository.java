package edu.ngd.repository;

import edu.ngd.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    Page<User> findByUsernameContainingAndIsDeleted(String keyword, Integer isDeleted, Pageable pageable);
    Page<User> findByIsDeleted(Integer isDeleted, Pageable pageable);
    Optional<User> findByUsernameAndIsDeleted(String username, Integer isDeleted);
}
