package edu.ngd.controller;

import edu.ngd.dto.response.ApiResponse;
import edu.ngd.dto.response.UserInfoResponse;
import edu.ngd.entity.User;
import edu.ngd.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(UserInfoResponse.fromUser(user)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserInfoResponse>>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserInfoResponse> responses = users.stream()
                .map(UserInfoResponse::fromUser)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("用户删除成功"));
    }

    @GetMapping("/{id}/storage")
    public ResponseEntity<ApiResponse<StorageInfo>> getUserStorage(@PathVariable Long id) {
        User user = userService.getUserById(id);
        StorageInfo storageInfo = StorageInfo.builder()
                .used(user.getStorageUsed())
                .quota(user.getStorageQuota())
                .build();
        return ResponseEntity.ok(ApiResponse.success(storageInfo));
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StorageInfo {
        private Long used;
        private Long quota;
    }
}