package com.hcmute.devhire.DTOs;

import lombok.*;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobApplicationWithScoreDTO {
    private Long applicationId;
    private String applicantName;
    private String applicantEmail;
    private double score;
    private Map<String, Double> scoreDetails;
}
