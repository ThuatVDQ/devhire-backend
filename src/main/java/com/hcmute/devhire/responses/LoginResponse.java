package com.hcmute.devhire.responses;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hcmute.devhire.DTOs.UserDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    @JsonProperty("message")
    private String message;

    @JsonProperty("token")
    private String token;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    @JsonProperty("role_id")
    private Long roleId;
}
