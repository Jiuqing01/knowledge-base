package edu.ngd.dto.response;

import edu.ngd.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    private Long id;
    private String username;
    private String role;
    private Long storageUsed;
    private Long storageQuota;

    public static UserInfoResponse fromUser(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .storageUsed(user.getStorageUsed())
                .storageQuota(user.getStorageQuota())
                .build();
    }
}