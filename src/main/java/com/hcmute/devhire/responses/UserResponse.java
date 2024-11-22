package com.hcmute.devhire.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hcmute.devhire.entities.User;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    @JsonProperty("full_name")
    private String fullName;

    private String email;

    private String phone;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    private String status;

    @JsonProperty("role_name")
    private String roleName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public static UserResponse convertFromUser(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .phone(user.getPhone())
            .avatarUrl(user.getAvatarUrl())
            .status(String.valueOf(user.getStatus()))
            .roleName(user.getRole().getName())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
