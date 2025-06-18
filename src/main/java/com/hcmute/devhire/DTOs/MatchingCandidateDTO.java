package com.hcmute.devhire.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatchingCandidateDTO {
    @JsonProperty("user_id")
    private Long userId;
    @JsonProperty("full_name")
    private String fullName;
    private String email;

    @JsonProperty("match_percentage")
    private double matchPercentage;
    @JsonProperty("cv_file_name")
    private String cvFileName;
    @JsonProperty("cv_file_url")
    private String cvFileUrl;
    @JsonProperty("matched_skills")
    private List<String> matchedSkills;
    @JsonProperty("latest_apply_date")
    private LocalDateTime latestApplyDate;
}
