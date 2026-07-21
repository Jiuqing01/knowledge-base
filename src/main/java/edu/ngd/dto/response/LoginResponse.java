package edu.ngd.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private Integer accessTokenExpireMinutes;
    private Integer refreshTokenExpireDays;
    private UserInfoResponse user;
}