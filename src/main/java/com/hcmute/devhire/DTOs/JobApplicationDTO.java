package com.hcmute.devhire.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobApplicationDTO {
    private Long id;
    private String status;
    @JsonProperty("job_id")
    private Long jobId;

    @JsonProperty("job_title")
    private String jobTitle;

    @JsonProperty("job_description")
    private String jobDescription;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("full_name")
    private String fullName;
    private String email;
    private String phone;

    @JsonProperty("cv_id")
    private Long cvId;

    @JsonProperty("cv_url")
    private String cvUrl;

    private LocalDateTime applyDate;

    @JsonProperty("is_scheduled")
    private Boolean isScheduled;
}
