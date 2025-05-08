package com.hcmute.devhire.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CvAnalysisResult {
    private Set<String> skills;
    private double yearsOfExperience;
    private List<String> education;
    private List<String> certifications;
    private List<String> jobTitles;
    private List<String> languages;
}
