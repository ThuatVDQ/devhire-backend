package com.hcmute.devhire.DTOs;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationDTO {

    @NotBlank(message = "Message cannot be blank")
    private String message;

    @NotBlank(message = "Username cannot be blank")
    private String username;

    @JsonProperty("target_url")
    private String targetUrl;
}
