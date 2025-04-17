package com.hcmute.devhire.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewScheduleUpdateDTO {
    @NotNull(message = "Interview time cannot be null")
    @Future(message = "Interview time must be in the future")
    @JsonProperty("interview_time")
    private LocalDateTime interviewTime;

    @Min(1)
    @Max(480)
    @JsonProperty("duration_minutes")
    private int durationMinutes;

    @NotBlank
    @Size(max = 255)
    private String location;

    @Size(max = 1000)
    private String note;
}
