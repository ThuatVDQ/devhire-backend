package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.CvAnalysisResult;
import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.repositories.JobRepository;
import com.hcmute.devhire.repositories.JobSkillRepository;
import com.hcmute.devhire.repositories.SkillRepository;
import com.hcmute.devhire.responses.CvScoreResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CvScoringService implements ICvScoringService {
    private final ICvAnalysisService cvAnalysisService;

    private final JobRepository jobRepository;

    private final SkillRepository skillRepository;
    private final JobSkillRepository jobSkillRepository;
    @Override
    public CvScoreResponse calculateCvSkillMatch(MultipartFile cvFile, Long jobId) throws IOException {
        CvAnalysisResult analysisResult = cvAnalysisService.analyzeCv(cvFile);
        Set<String> cvSkills = analysisResult.getSkills();

        CvScoreResponse response = new CvScoreResponse();
        response.setCvAnalysis(analysisResult);

        if (jobId == null || cvSkills == null || cvSkills.isEmpty()) {
            response.setTotalScore(0.0);
            response.setMatchedSkills(Collections.emptyList());
            return response;
        }

        List<String> jobSkills = jobSkillRepository.findAllSkillNameByJobId(jobId);
        if (jobSkills == null || jobSkills.isEmpty()) {
            response.setTotalScore(0.0);
            response.setMatchedSkills(Collections.emptyList());
            return response;
        }

        Set<String> jobSkillsLower = jobSkills.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        List<String> matchedSkills = cvSkills.stream()
                .filter(cvSkill -> jobSkillsLower.contains(cvSkill.toLowerCase()))
                .collect(Collectors.toList());

        double percentage = Math.round(((double) matchedSkills.size() / jobSkills.size()) * 10000.0) / 100.0;

        response.setTotalScore(percentage);
        response.setMatchedSkills(matchedSkills);
        return response;
    }
}
