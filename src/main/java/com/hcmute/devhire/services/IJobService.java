package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.ApplyJobRequestDTO;
import com.hcmute.devhire.DTOs.JobApplicationDTO;
import com.hcmute.devhire.DTOs.JobDTO;
import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.entities.JobApplication;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.responses.JobListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface IJobService {
    Job createJob(JobDTO jobDTO, String username) throws Exception;
    Page<JobDTO> getAllJobs(PageRequest pageRequest) throws Exception;
    Job findById(Long jobId) throws Exception;
    JobApplication applyForJob(Long jobId, ApplyJobRequestDTO applyJobRequestDTO) throws Exception;

    List<JobDTO> getJobsByCompany(String username) throws Exception;
    void likeJob(Long jobId, String username) throws Exception;
    JobListResponse getFavoriteJobs(User user) throws Exception;

}
