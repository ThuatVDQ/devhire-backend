package com.hcmute.devhire.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class InterviewScheduleBulkDTO {
    @NotEmpty(message = "Job application IDs cannot be empty")
    @JsonProperty("job_application_ids")
    private List<Long> jobApplicationIds;

    @NotNull
    @Future
    @JsonProperty("interview_time")
    private LocalDateTime interviewTime;

    @Min(1) @Max(480)
    @JsonProperty("duration_minutes")
    private int durationMinutes;

    @NotBlank @Size(max = 255)
    private String location;

    @Size(max = 1000)
    private String note;
}
