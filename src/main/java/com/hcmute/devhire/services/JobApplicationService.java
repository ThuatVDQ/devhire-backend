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
    public List<JobApplication> findByJobId(Long jobId) {
        return jobApplicationRepository.findByJobId(jobId);
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
