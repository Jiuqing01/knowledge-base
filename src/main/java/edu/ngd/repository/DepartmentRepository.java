package edu.ngd.repository;

import edu.ngd.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByParentId(Long parentId);
    List<Department> findByParentIdOrderBySortOrderAsc(Long parentId);
    List<Department> findAllByOrderBySortOrderAsc();
    boolean existsByName(String name);
    boolean existsByNameAndParentId(String name, Long parentId);
}