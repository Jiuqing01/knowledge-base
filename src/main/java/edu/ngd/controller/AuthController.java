package edu.ngd.controller;

import edu.ngd.dto.request.LoginRequest;
import edu.ngd.dto.request.RegisterRequest;
import edu.ngd.dto.request.ResetPasswordRequest;
import edu.ngd.dto.request.SendResetCodeRequest;
import edu.ngd.dto.response.ApiResponse;
import edu.ngd.dto.response.LoginResponse;
import edu.ngd.dto.response.UserInfoResponse;
import edu.ngd.entity.User;
import edu.ngd.repository.UserRepository;
import edu.ngd.service.AuthService;
import edu.ngd.service.VerificationCodeService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final VerificationCodeService verificationCodeService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserInfoResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request received: username={}, role={}", request.getUsername(), request.getRole());
        User user = authService.register(request);
        log.info("User registered successfully: id={}, username={}", user.getId(), user.getUsername());
        return ResponseEntity.ok(ApiResponse.success("注册成功", UserInfoResponse.fromUser(user)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        authService.logout(authorizationHeader);
        return ResponseEntity.ok(ApiResponse.success("登出成功"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        LoginResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody Map<String, String> request) {
        Long userId = Long.parseLong(request.get("userId"));
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        
        authService.changePassword(userId, oldPassword, newPassword);
        return ResponseEntity.ok(ApiResponse.success("密码修改成功"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser(@RequestParam Long userId) {
        User user = authService.getById(userId);
        return ResponseEntity.ok(ApiResponse.success(UserInfoResponse.fromUser(user)));
    }

    @PostMapping("/send-reset-code")
    public ResponseEntity<ApiResponse<Void>> sendResetCode(@Valid @RequestBody SendResetCodeRequest request) {
        verificationCodeService.sendCode(request.getPhone());
        return ResponseEntity.ok(ApiResponse.success("验证码已发送"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        if (!verificationCodeService.verifyCode(request.getPhone(), request.getCode())) {
            throw new RuntimeException("验证码无效或已过期");
        }
        
        Optional<User> userOptional = userRepository.findByUsername(request.getPhone());
        if (!userOptional.isPresent()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        
        return ResponseEntity.ok(ApiResponse.success("密码重置成功"));
    }
}