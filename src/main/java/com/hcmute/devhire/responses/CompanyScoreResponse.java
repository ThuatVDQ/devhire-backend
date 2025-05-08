package com.hcmute.devhire.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class CompanyScoreResponse {
    private Long companyId;
    private String companyName;
    private double totalScore; // 0-100
    private double starRating; // 0-5
    private Map<String, Double> scoreDetails;
}
