package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.JobApplicationDTO;
import com.hcmute.devhire.entities.JobApplication;

import java.util.List;

public interface IJobApplicationService {
    List<JobApplication> findByJobId(Long jobId);
    List<JobApplication> findByUserId(Long userId);
    JobApplication findByJobIdAndUserId(Long jobId, Long userId);
    void deleteByJobIdAndUserId(Long jobId, Long userId);
    void deleteByJobId(Long jobId);
    void deleteByUserId(Long userId);

    JobApplication applyJob(JobApplicationDTO jobApplicationDTO) throws Exception;
}
