package edu.ngd.service;

import edu.ngd.entity.User;
import edu.ngd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateUser(Long id, String username, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (username != null && !username.isEmpty()) {
            user.setUsername(username);
        }
        if (role != null) {
            user.setRole(edu.ngd.entity.UserRole.valueOf(role));
        }

        User updatedUser = userRepository.save(user);
        log.info("Updated user: id={}, username={}", updatedUser.getId(), updatedUser.getUsername());
        return updatedUser;
    }

    @Transactional
    public void updateStorageUsed(Long userId, Long delta) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Long newStorageUsed = user.getStorageUsed() + delta;
        if (newStorageUsed < 0) {
            newStorageUsed = 0L;
        }
        user.setStorageUsed(newStorageUsed);
        userRepository.save(user);

        log.info("Updated storage for user {}: {} bytes", userId, newStorageUsed);
    }

    public boolean hasEnoughStorage(Long userId, Long fileSize) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        return user.getStorageUsed() + fileSize <= user.getStorageQuota();
    }

    public Long getAvailableStorage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        return user.getStorageQuota() - user.getStorageUsed();
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("用户不存在");
        }
        userRepository.deleteById(id);
        log.info("Deleted user: id={}", id);
    }
}