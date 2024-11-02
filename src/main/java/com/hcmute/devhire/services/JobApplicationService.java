package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.JobApplicationDTO;
import com.hcmute.devhire.entities.CV;
import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.entities.JobApplication;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.exceptions.DataNotFoundException;
import com.hcmute.devhire.repositories.CVRepository;
import com.hcmute.devhire.repositories.JobApplicationRepository;
import com.hcmute.devhire.repositories.JobRepository;
import com.hcmute.devhire.repositories.UserRepository;
import com.hcmute.devhire.utils.JobApplicationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobApplicationService implements IJobApplicationService{
    private final JobApplicationRepository jobApplicationRepository;


    @Override
    public JobApplicationDTO getJobApplication(Long jobApplicationId) throws DataNotFoundException {
        JobApplication jobApplication = jobApplicationRepository.findById(jobApplicationId)
                .orElseThrow(() ->
                        new DataNotFoundException("Job application not found with id: " + jobApplicationId));
        return JobApplicationDTO.builder()
                .status(jobApplication.getStatus().name())
                .jobId(jobApplication.getJob().getId())
                .userId(jobApplication.getUser().getId())
                .cvId(jobApplication.getCv().getId())
                .build();
    }

    @Override
    public List<String> getAllCvPathsByJobId(Long jobId) {
        List<JobApplication> jobApplications = jobApplicationRepository.findByJobId(jobId);
        return jobApplications.stream()
                .map(JobApplication::getCv)
                .filter(Objects::nonNull)
                .map(CV::getCvUrl)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<JobApplication> findApplicationByJobIdAndUserId(Long jobId, Long userId) {
        JobApplication jobApplications = jobApplicationRepository.findByJobIdAndUserId(jobId, userId);
        if (jobApplications != null) {
            return Optional.of(jobApplications);
        }
        return Optional.empty();
    }

    @Override
    public void updateJobApplication(JobApplication jobApplication) {
        jobApplicationRepository.save(jobApplication);
    }

    @Override
    public void seenJobApplication(Long jobApplicationId) {
        try {
            JobApplication jobApplication = jobApplicationRepository.findById(jobApplicationId).get();
            jobApplication.setStatus(JobApplicationStatus.SEEN);
            jobApplicationRepository.save(jobApplication);
        } catch (Exception e) {
            throw new RuntimeException("Job application not found with id: " + jobApplicationId);
        }
    }

    @Override
    public void seenAllJobApplication(Long jobId) {
        List<JobApplication> jobApplications = jobApplicationRepository.findByJobId(jobId);
        if (jobApplications.isEmpty()) {
            throw new RuntimeException("Job applications not found");
        }
        jobApplications.forEach(jobApplication -> {
            if (jobApplication.getStatus().equals(JobApplicationStatus.IN_PROGRESS)) {
                jobApplication.setStatus(JobApplicationStatus.SEEN);
                jobApplicationRepository.save(jobApplication);
            }
        });
    }

    @Override
    public void rejectJobApplication(Long jobApplicationId) {
        try {
            JobApplication jobApplication = jobApplicationRepository.findById(jobApplicationId).get();
            jobApplication.setStatus(JobApplicationStatus.REJECTED);
            jobApplicationRepository.save(jobApplication);
        } catch (Exception e) {
            throw new RuntimeException("Job application not found with id: " + jobApplicationId);
        }
    }

    @Override
    public void approveJobApplication(Long jobApplicationId) {
        try {
            JobApplication jobApplication = jobApplicationRepository.findById(jobApplicationId).get();
            jobApplication.setStatus(JobApplicationStatus.ACCEPTED);
            jobApplicationRepository.save(jobApplication);
        } catch (Exception e) {
            throw new RuntimeException("Job application not found with id: " + jobApplicationId);
        }
    }

    @Override
    public List<JobApplicationDTO> findByJobId(Long jobId) {
        List<JobApplication> jobApplications = jobApplicationRepository.findByJobId(jobId);
        return jobApplications.stream().map(app -> JobApplicationDTO.builder()
                .status(app.getStatus().name())
                .jobId(app.getJob().getId())
                .jobTitle(app.getJob().getTitle())
                .fullName(app.getUser().getFullName())
                .cvId(app.getCv().getId())
                .cvUrl(app.getCv().getCvUrl())
                .applyDate(app.getUpdatedAt())
                .id(app.getId())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public List<JobApplication> findByUserId(Long userId) {
        return jobApplicationRepository.findByUserId(userId);
    }

    @Override
    public JobApplication findByJobIdAndUserId(Long jobId, Long userId) {
        return jobApplicationRepository.findByJobIdAndUserId(jobId, userId);
    }

    @Override
    public void deleteByJobIdAndUserId(Long jobId, Long userId) {
        jobApplicationRepository.deleteByJobIdAndUserId(jobId, userId);
    }

    @Override
    public void deleteByJobId(Long jobId) {
        jobApplicationRepository.deleteByJobId(jobId);
    }

    @Override
    public void deleteByUserId(Long userId) {
        jobApplicationRepository.deleteByUserId(userId);
    }

}
