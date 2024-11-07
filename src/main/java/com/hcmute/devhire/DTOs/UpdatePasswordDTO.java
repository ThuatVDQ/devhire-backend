package com.hcmute.devhire.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePasswordDTO {
    @NotBlank(message = "Password can't be blank")
    private String password;
    @NotBlank(message = "New password can't be blank")
    private String newPassword;
    @NotBlank(message = "Confirm password can't be blank")
    private String confirmPassword;

    public boolean isPasswordMatching() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }

}
