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
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final CVRepository cvRepository;
    @Override
    public JobApplication applyJob(JobApplicationDTO jobApplicationDTO) throws Exception {
        User user = userRepository.findById(jobApplicationDTO.getUserId())
                .orElseThrow(() ->
                        new DataNotFoundException("User not found with id: " + jobApplicationDTO.getUserId()));
        Job job = jobRepository.findById(jobApplicationDTO.getJobId())
                .orElseThrow(() ->
                        new DataNotFoundException("Job not found with id: " + jobApplicationDTO.getJobId()));
        CV cv = cvRepository.findById(jobApplicationDTO.getCvId())
                .orElseThrow(() ->
                        new DataNotFoundException("CV not found with id: " + jobApplicationDTO.getCvId()));
        JobApplication jobApplication = new JobApplication();
        jobApplication.setJob(job);
        jobApplication.setUser(user);
        jobApplication.setCv(cv);
        jobApplication.setStatus(JobApplicationStatus.valueOf(jobApplicationDTO.getStatus()));

        return jobApplicationRepository.save(jobApplication);
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
