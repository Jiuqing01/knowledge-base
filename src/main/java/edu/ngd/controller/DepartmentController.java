package edu.ngd.controller;

import edu.ngd.dto.response.ApiResponse;
import edu.ngd.entity.Department;
import edu.ngd.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Department>>> getAllDepartments() {
        List<Department> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<Department>>> getDepartmentTree() {
        List<Department> departments = departmentService.getDepartmentTree();
        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    @GetMapping("/root")
    public ResponseEntity<ApiResponse<List<Department>>> getRootDepartments() {
        List<Department> departments = departmentService.getRootDepartments();
        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    @GetMapping("/children/{parentId}")
    public ResponseEntity<ApiResponse<List<Department>>> getChildDepartments(@PathVariable Long parentId) {
        List<Department> departments = departmentService.getChildDepartments(parentId);
        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Department>> getDepartment(@PathVariable Long id) {
        Department department = departmentService.getDepartment(id);
        return ResponseEntity.ok(ApiResponse.success(department));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Department>> createDepartment(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        Long parentId = request.get("parentId") != null ? ((Number) request.get("parentId")).longValue() : null;
        Integer sortOrder = request.get("sortOrder") != null ? ((Number) request.get("sortOrder")).intValue() : 0;
        
        Department department = departmentService.createDepartment(name, parentId, sortOrder);
        return ResponseEntity.ok(ApiResponse.success("部门创建成功", department));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Department>> updateDepartment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        Long parentId = request.get("parentId") != null ? ((Number) request.get("parentId")).longValue() : null;
        
        Department department = departmentService.updateDepartment(id, name, parentId);
        return ResponseEntity.ok(ApiResponse.success("部门更新成功", department));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success("部门删除成功"));
    }
}