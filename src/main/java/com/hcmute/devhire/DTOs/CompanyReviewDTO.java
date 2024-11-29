package com.hcmute.devhire.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyReviewDTO {

    @JsonProperty("full_name")
    private String fullName;

    @NotBlank
    private String comment;

    private int rating;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
