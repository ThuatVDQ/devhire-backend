package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.JobApplicationDTO;
import com.hcmute.devhire.entities.JobApplication;
import com.hcmute.devhire.exceptions.DataNotFoundException;

import java.util.List;
import java.util.Optional;

public interface IJobApplicationService {
    List<JobApplicationDTO> findByJobId(Long jobId);
    List<JobApplication> findByUserId(Long userId);
    JobApplication findByJobIdAndUserId(Long jobId, Long userId);
    void deleteByJobIdAndUserId(Long jobId, Long userId);
    void deleteByJobId(Long jobId);
    void deleteByUserId(Long userId);

    JobApplicationDTO getJobApplication(Long jobApplicationId) throws DataNotFoundException;
    List<String> getAllCvPathsByJobId(Long jobId);
    Optional<JobApplication> findApplicationByJobIdAndUserId(Long jobId, Long userId);
    void updateJobApplication(JobApplication jobApplication);
    void seenJobApplication(Long jobApplicationId);
    void rejectJobApplication(Long jobApplicationId);
    void approveJobApplication(Long jobApplicationId);
}
