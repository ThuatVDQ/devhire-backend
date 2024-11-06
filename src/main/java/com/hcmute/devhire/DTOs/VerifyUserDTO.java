package com.hcmute.devhire.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerifyUserDTO {
    @NotBlank(message = "Email can't be blank")
    private String email;

    @NotBlank(message = "Verification code can't be blank")
    @JsonProperty("verification_code")
    private String verificationCode;
}
