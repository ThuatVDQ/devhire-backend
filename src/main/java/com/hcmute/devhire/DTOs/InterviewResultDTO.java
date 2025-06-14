package com.hcmute.devhire.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hcmute.devhire.utils.InterviewResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InterviewResultDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("recruiter_note")
    private String recruiterNote;

    @JsonProperty("email_sent")
    private boolean emailSent;

    private InterviewResult result;
}
