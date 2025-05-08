package com.hcmute.devhire.responses;

import com.hcmute.devhire.DTOs.CvAnalysisResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class CvScoreResponse {
    private CvAnalysisResult cvAnalysis;
    private double totalScore; // Điểm tổng (0-100)
    private Map<String, Double> scoreDetails; // Điểm chi tiết các mục
}
