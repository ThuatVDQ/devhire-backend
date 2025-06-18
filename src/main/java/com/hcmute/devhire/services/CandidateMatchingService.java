package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.CvAnalysisResult;
import com.hcmute.devhire.DTOs.MatchingCandidateDTO;
import com.hcmute.devhire.entities.*;
import com.hcmute.devhire.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CandidateMatchingService implements ICandidateMatchingService {
    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final CVRepository cvRepository;
    private final UserRepository userRepository;
    private final JobSkillRepository jobSkillRepository;
    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    @Override
    public List<MatchingCandidateDTO> findMatchingCandidates(Long jobId, double threshold) throws IOException {
        Optional<Job> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            throw new RuntimeException("Job not found with ID: " + jobId);
        }

        List<String> jobSkills = jobSkillRepository.findAllSkillNameByJobId(jobId);
        if (jobSkills.isEmpty()) return Collections.emptyList();

        List<CV> allCvs = cvRepository.findAll();
        Set<Long> userIds = allCvs.stream()
                .map(cv -> cv.getUser().getId())
                .collect(Collectors.toSet());

        List<MatchingCandidateDTO> result = new ArrayList<>();

        for (Long userId : userIds) {
            List<UserSkill> userSkills = userSkillRepository.findByUserId(userId);
            CV cv = cvRepository.findTopByUserIdOrderByUpdatedAtDesc(userId);
            Set<String> lowerCvSkills = userSkills.stream()
                    .map(userSkill -> {
                        Skill skill = userSkill.getSkill();
                        return skill != null ? skill.getName().toLowerCase() : "";
                    })
                    .collect(Collectors.toSet());

            List<String> matchedSkills = jobSkills.stream()
                    .filter(skill -> lowerCvSkills.contains(skill.toLowerCase()))
                    .collect(Collectors.toList());

            double matchPercent = jobSkills.isEmpty() ? 0.0 : (double) matchedSkills.size() / jobSkills.size() * 100;
            matchPercent = Math.round(matchPercent * 100.0) / 100.0;

            if (matchPercent >= threshold) {
                User user = userRepository.findById(userId).orElse(null);
                if (user == null) continue;

                // Lấy ngày apply gần nhất nếu có
                LocalDateTime latestApplyDate = jobApplicationRepository
                        .findLatestApplyDateByUserId(userId)
                        .orElse(null);

                MatchingCandidateDTO dto = MatchingCandidateDTO.builder()
                        .userId(userId)
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .matchPercentage(matchPercent)
                        .cvFileName(Paths.get(cv.getCvUrl()).getFileName().toString())
                        .cvFileUrl(cv.getCvUrl())
                        .matchedSkills(matchedSkills)
                        .latestApplyDate(latestApplyDate)
                        .build();
                result.add(dto);
            }
        }

        result.sort(Comparator.comparingDouble(MatchingCandidateDTO::getMatchPercentage).reversed());
        return result;
    }
}
