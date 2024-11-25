package com.hcmute.devhire.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplyJobRequestDTO {
    @JsonProperty("job_id")
    private Long jobId;
    @JsonProperty("user_id")
    private Long userId;
    @JsonProperty("cv_id")
    private Long cvId;

    private String letter;
}