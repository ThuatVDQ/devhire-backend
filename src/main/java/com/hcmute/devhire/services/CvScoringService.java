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

@Service
@RequiredArgsConstructor
public class CvScoringService implements ICvScoringService {
    private final CvAnalysisService cvAnalysisService;

    private final JobRepository jobRepository;

    private final SkillRepository skillRepository;
    private final JobSkillRepository jobSkillRepository;
    @Override
    public CvScoreResponse calculateCvScore(MultipartFile cvFile, Long jobId) throws IOException {
        // Phân tích CV
        CvAnalysisResult cvAnalysis = cvAnalysisService.analyzeCv(cvFile);

        // Tạo response
        CvScoreResponse response = new CvScoreResponse();
        response.setCvAnalysis(cvAnalysis);

        // Tính điểm tổng
        double totalScore = 0;
        Map<String, Double> scoreDetails = new HashMap<>();

        // 1. Điểm kỹ năng (40%)
        double skillScore = calculateSkillScore(cvAnalysis.getSkills(), jobId);
        scoreDetails.put("skills", skillScore);
        totalScore += skillScore * 0.4;

        // 2. Điểm kinh nghiệm (30%)
        double experienceScore = calculateExperienceScore(cvAnalysis.getYearsOfExperience(), jobId);
        scoreDetails.put("experience", experienceScore);
        totalScore += experienceScore * 0.3;

        // 3. Điểm học vấn (20%)
        double educationScore = calculateEducationScore(cvAnalysis.getEducation(), jobId);
        scoreDetails.put("education", educationScore);
        totalScore += educationScore * 0.2;

        // 4. Điểm chứng chỉ (10%)
        double certificationScore = calculateCertificationScore(cvAnalysis.getCertifications(), jobId);
        scoreDetails.put("certifications", certificationScore);
        totalScore += certificationScore * 0.1;

        response.setTotalScore(totalScore);
        response.setScoreDetails(scoreDetails);

        return response;
    }

    private double calculateSkillScore(Set<String> cvSkills, Long jobId) {
        if (jobId != null) {
            // Tính điểm dựa trên yêu cầu công việc cụ thể
            List<String> jobSkills = jobSkillRepository.findAllSkillNameByJobId(jobId);
            long matchedSkills = cvSkills.stream()
                    .filter(jobSkills::contains)
                    .count();

            return jobSkills.isEmpty() ? 0 : (double) matchedSkills / jobSkills.size();
        }

        // Tính điểm chung nếu không có job cụ thể
        return cvSkills.size() / 10.0; // Chuẩn hóa về thang điểm 1.0
    }

    private double calculateExperienceScore(double yearsOfExperience, Long jobId) {
        if (jobId != null) {
            Optional<Job> job = jobRepository.findById(jobId);
            if (job.isPresent()) {
                double jobExperience = parseExperienceString(job.get().getExperience());
                if (jobExperience > 0) {
                    return Math.min(yearsOfExperience / jobExperience, 1.0);
                }
            }
        }

        return Math.min(yearsOfExperience / 10.0, 1.0); // Tối đa 10 năm = điểm 1.0
    }

    private double parseExperienceString(String experienceStr) {
        if (experienceStr == null || experienceStr.isEmpty()) return 0;

        experienceStr = experienceStr.toLowerCase().trim();

        // Trường hợp "2-4 years"
        if (experienceStr.matches(".*\\d+\\s*-\\s*\\d+.*")) {
            Matcher matcher = Pattern.compile("(\\d+)\\s*-\\s*(\\d+)").matcher(experienceStr);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }

        // Trường hợp "3+ years"
        if (experienceStr.matches(".*\\d+\\+.*")) {
            Matcher matcher = Pattern.compile("(\\d+)\\+").matcher(experienceStr);
            if (matcher.find()) {
                return Double.parseDouble(matcher.group(1));
            }
        }

        // Trường hợp "3 years", "5 năm"
        Matcher matcher = Pattern.compile("(\\d+)").matcher(experienceStr);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }

        return 0;
    }

    private double calculateEducationScore(List<String> educations, Long jobId) {
        if (educations == null || educations.isEmpty()) {
            return 0.0;
        }

        // Ở đây tạm tính theo số lượng mục học vấn
        return Math.min(educations.size() / 3.0, 1.0); // Giả sử tối đa 3 trình độ = điểm 1.0
    }

    private double calculateCertificationScore(List<String> certifications, Long jobId) {
        if (certifications == null || certifications.isEmpty()) {
            return 0.0;
        }

        return Math.min(certifications.size() / 5.0, 1.0); // Tối đa 5 chứng chỉ = điểm 1.0
    }
}
