package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.JobDTO;
import com.hcmute.devhire.entities.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface IJobService {
    Job createJob(JobDTO jobDTO);
    Page<Job> getAllJobs(PageRequest pageRequest);
    Job findById(Long jobId);
}
