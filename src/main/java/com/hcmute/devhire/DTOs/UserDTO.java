package com.hcmute.devhire.DTOs;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hcmute.devhire.utils.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Long id;
    @JsonProperty("full_name")
    private String fullName;

    private String email;

    @NotBlank(message = "Phone can't be blank")
    private String phone;

    @NotBlank(message = "Password can't be blank")
    private String password;

    @JsonProperty("retype_password")
    private String retypePassword;

    private String gender;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    private String introduction;

    private String status;
    @JsonProperty("role_id")
    @NotNull(message = "Role ID is required")
    private Long roleId;
}
