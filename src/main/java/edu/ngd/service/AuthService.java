package edu.ngd.service;

import edu.ngd.config.JwtConfig;
import edu.ngd.dto.request.LoginRequest;
import edu.ngd.dto.request.RegisterRequest;
import edu.ngd.dto.response.LoginResponse;
import edu.ngd.dto.response.UserInfoResponse;
import edu.ngd.entity.User;
import edu.ngd.entity.UserRole;
import edu.ngd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final FolderTemplateService folderTemplateService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        UserRole role = request.getRole() != null ? request.getRole() : UserRole.USER;
        
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .storageQuota(jwtConfig.getAccessTokenExpireMinutes() > 0 ? 1073741824L : 1073741824L)
                .storageUsed(0L)
                .build();

        User savedUser = userRepository.save(user);
        
        folderTemplateService.createFoldersForUser(savedUser.getId());
        
        log.info("User registered: {}", savedUser.getUsername());
        return savedUser;
    }

    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());
        User user = userRepository.findByUsernameAndIsDeleted(request.getUsername(), 0)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", request.getUsername());
                    return new RuntimeException("Invalid username or password");
                });
        
        log.info("User found: id={}, username={}, storedPassword={}", user.getId(), user.getUsername(), user.getPassword());
        log.info("Raw password from request: {}", request.getPassword());
        
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        log.info("Password matches: {}", passwordMatches);

        if (!passwordMatches) {
            throw new RuntimeException("Invalid username or password");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("User logged in: {}", user.getUsername());
        
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpireMinutes(jwtConfig.getAccessTokenExpireMinutes())
                .refreshTokenExpireDays(jwtConfig.getRefreshTokenExpireDays())
                .user(UserInfoResponse.fromUser(user))
                .build();
    }

    public void logout(String accessToken) {
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }
        
        Long userId = jwtService.getUserIdFromToken(accessToken);
        tokenBlacklistService.addToBlacklist(accessToken, userId);
        log.info("User logged out: userId={}", userId);
    }

    public LoginResponse refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new RuntimeException("Refresh token is required");
        }

        try {
            Long userId = jwtService.getUserIdFromToken(refreshToken);
            String username = jwtService.getUsernameFromToken(refreshToken);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String newAccessToken = jwtService.generateAccessToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            return LoginResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .accessTokenExpireMinutes(jwtConfig.getAccessTokenExpireMinutes())
                    .refreshTokenExpireDays(jwtConfig.getRefreshTokenExpireDays())
                    .user(UserInfoResponse.fromUser(user))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Invalid refresh token", e);
        }
    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenBlacklistService.invalidateUserTokens(userId);
        log.info("Password changed for user: {}", user.getUsername());
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User getById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}