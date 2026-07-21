package edu.ngd.dto.request;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Verification code is required")
    private String code;

    @NotBlank(message = "Password is required")
    private String password;
}