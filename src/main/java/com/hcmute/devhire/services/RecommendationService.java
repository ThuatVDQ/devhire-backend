package com.hcmute.devhire.services;

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
    private static final int SKILL_VECTOR_SIZE = 20;
    public List<Job> recommendJobsForUser(String email, int topK) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<JobStatus> statuses = Arrays.asList(JobStatus.OPEN, JobStatus.HOT);
        List<Job> allJobs = jobRepository.findByStatusIn(statuses);
        double[] userVector = encodeUserPreferences(user);
        EuclideanDistance distance = new EuclideanDistance();

       return allJobs.stream()
                .sorted(Comparator.comparingDouble(
                        job -> distance.d(userVector, encodeJobFeatures(job))
                ))
                .limit(topK)
                .collect(Collectors.toList());
    }

    private double[] encodeJobFeatures(Job job) {
        double[] vector = new double[3 + SKILL_VECTOR_SIZE];
        vector[0] = job.getCategory() != null ? job.getCategory().getId() : 0;
        vector[1] = job.getSalaryStart() != null ? job.getSalaryStart() : 0;
        vector[2] = job.getSalaryEnd() != null ? job.getSalaryEnd() : 0;

        for (JobSkill js : job.getJobSkills()) {
            int index = js.getSkill().getId().intValue() - 1;
            if (index >= 0 && index < SKILL_VECTOR_SIZE) {
                vector[3 + index] = 1;
            }
        }
        return vector;
    }

    private double[] encodeUserPreferences(User user) {
        double[] vector = new double[3 + SKILL_VECTOR_SIZE];
        List<UserSkill> userSkills = userSkillRepository.findByUserId(user.getId());

        List<Skill> skills = userSkills.stream()
                .map(UserSkill::getSkill)
                .toList();

        for (Skill skill : skills) {
            int index = skill.getId().intValue() - 1;
            if (index >= 0 && index < SKILL_VECTOR_SIZE) {
                vector[3 + index] = 1;
            }
        }
        return vector;
    }
}
