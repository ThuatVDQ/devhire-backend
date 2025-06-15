package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.JobDTO;
import com.hcmute.devhire.entities.*;
import com.hcmute.devhire.repositories.JobRepository;
import com.hcmute.devhire.repositories.UserRepository;
import com.hcmute.devhire.repositories.UserSkillRepository;
import com.hcmute.devhire.utils.JobStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import smile.math.distance.EuclideanDistance;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class RecommendationService implements IRecommendationService {
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final IJobService jobService;

    private static final int SKILL_VECTOR_SIZE = 20;

    public List<JobDTO> recommendJobsForUser(String email, int topK) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<JobStatus> statuses = Arrays.asList(JobStatus.OPEN, JobStatus.HOT);
        List<Job> allJobs = jobRepository.findByStatusIn(statuses);
        double[] userVector = encodeUserSkillVector(user);
        EuclideanDistance distance = new EuclideanDistance();

        return allJobs.stream()
                .sorted(Comparator.comparingDouble(
                        job -> distance.d(userVector, encodeJobSkillVector(job))
                ))
                .limit(topK)
                .map(job -> {
                    try {
                        return jobService.convertDTO(job, user.getUsername());
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to convert job to DTO", e);
                    }
                })
                .collect(Collectors.toList());
    }

    private double[] encodeJobSkillVector(Job job) {
        double[] vector = new double[SKILL_VECTOR_SIZE];
        for (JobSkill js : job.getJobSkills()) {
            int index = js.getSkill().getId().intValue() - 1;
            if (index >= 0 && index < SKILL_VECTOR_SIZE) {
                vector[index] = 1;
            }
        }
        return vector;
    }

    private double[] encodeUserSkillVector(User user) {
        double[] vector = new double[SKILL_VECTOR_SIZE];
        List<UserSkill> userSkills = userSkillRepository.findByUserId(user.getId());

        for (UserSkill us : userSkills) {
            int index = us.getSkill().getId().intValue() - 1;
            if (index >= 0 && index < SKILL_VECTOR_SIZE) {
                vector[index] = 1;
            }
        }
        return vector;
    }
}
