package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.ApplyJobRequestDTO;
import com.hcmute.devhire.DTOs.JobApplicationDTO;
import com.hcmute.devhire.DTOs.JobDTO;
import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.entities.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface IJobService {
    Job createJob(JobDTO jobDTO);
    List<Job> getAllJobs();
    Job findById(Long jobId);
    JobApplication applyForJob(Long jobId, ApplyJobRequestDTO applyJobRequestDTO) throws Exception;
}
