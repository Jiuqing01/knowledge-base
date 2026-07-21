package edu.ngd.service;

import edu.ngd.entity.Department;
import edu.ngd.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional
    public Department createDepartment(String name, Long parentId, Integer sortOrder) {
        if (parentId != null && !departmentRepository.existsById(parentId)) {
            throw new RuntimeException("上级部门不存在");
        }

        if (departmentRepository.existsByNameAndParentId(name, parentId)) {
            throw new RuntimeException("部门名称已存在");
        }

        Department department = Department.builder()
                .name(name)
                .parentId(parentId)
                .sortOrder(sortOrder != null ? sortOrder : 0)
                .build();

        Department saved = departmentRepository.save(department);
        log.info("Created department: id={}, name={}, parentId={}", saved.getId(), name, parentId);
        return saved;
    }

    @Transactional
    public Department updateDepartment(Long id, String name, Long parentId) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("部门不存在"));

        if (name != null && !name.isEmpty()) {
            if (!name.equals(department.getName()) && 
                departmentRepository.existsByNameAndParentId(name, parentId)) {
                throw new RuntimeException("部门名称已存在");
            }
            department.setName(name);
        }

        if (parentId != null) {
            if (!departmentRepository.existsById(parentId)) {
                throw new RuntimeException("上级部门不存在");
            }
            department.setParentId(parentId);
        }

        Department saved = departmentRepository.save(department);
        log.info("Updated department: id={}, name={}", id, name);
        return saved;
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("部门不存在"));

        List<Department> children = departmentRepository.findByParentId(id);
        if (!children.isEmpty()) {
            throw new RuntimeException("请先删除子部门");
        }

        departmentRepository.delete(department);
        log.info("Deleted department: id={}, name={}", id, department.getName());
    }

    public Department getDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("部门不存在"));
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAllByOrderBySortOrderAsc();
    }

    public List<Department> getRootDepartments() {
        return departmentRepository.findByParentIdOrderBySortOrderAsc(null);
    }

    public List<Department> getChildDepartments(Long parentId) {
        return departmentRepository.findByParentIdOrderBySortOrderAsc(parentId);
    }

    public List<Department> getDepartmentTree() {
        List<Department> allDepartments = getAllDepartments();
        return buildTree(null, allDepartments);
    }

    private List<Department> buildTree(Long parentId, List<Department> allDepartments) {
        List<Department> tree = new ArrayList<>();
        for (Department dept : allDepartments) {
            if (dept.getParentId() == parentId) {
                tree.add(dept);
            }
        }
        return tree;
    }
}