package com.hcmute.devhire.responses;

import com.hcmute.devhire.DTOs.CvAnalysisResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class CvScoreResponse {
    private CvAnalysisResult cvAnalysis;
    private double totalScore; // phần trăm kỹ năng phù hợp
    private List<String> matchedSkills; // Điểm chi tiết các mục
}
