package com.hcmute.devhire.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hcmute.devhire.utils.JobApplicationStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InterviewScheduleDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("job_application_id")
    @NotNull(message = "Job application ID cannot be null")
    private Long jobApplicationId;

    @JsonProperty("interview_time")
    @NotNull(message = "Interview time cannot be null")
    @Future(message = "Interview time must be in the future")
    private LocalDateTime interviewTime;

    @JsonProperty("duration_minutes")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 480, message = "Duration cannot exceed 8 hours (480 minutes)")
    private int durationMinutes;

    @JsonProperty("location")
    @NotBlank(message = "Location cannot be blank")
    @Size(max = 255, message = "Location cannot exceed 255 characters")
    private String location;

    @JsonProperty("note")
    @Size(max = 1000, message = "Note cannot exceed 1000 characters")
    private String note;

    // Thông tin ứng viên
    @JsonProperty("candidate_name")
    private String candidateName;

    @JsonProperty("candidate_email")
    private String candidateEmail;

    // Thông tin công việc
    @JsonProperty("job_title")
    private String jobTitle;

    @JsonProperty("job_id")
    private Long jobId;

    // Trạng thái ứng tuyển
    @JsonProperty("application_status")
    private JobApplicationStatus applicationStatus;
}
