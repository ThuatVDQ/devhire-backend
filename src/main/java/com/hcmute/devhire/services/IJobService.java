package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.ApplyJobRequestDTO;
import com.hcmute.devhire.DTOs.JobDTO;
import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.entities.JobApplication;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.responses.JobListResponse;
import com.hcmute.devhire.responses.MonthlyCountResponse;
import com.hcmute.devhire.utils.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface IJobService {
    Job createJob(JobDTO jobDTO, String username) throws Exception;
    Page<JobDTO> getAllJobs(PageRequest pageRequest, String username) throws Exception;
    Page<JobDTO> getAllJobsAdmin(PageRequest pageRequest, String status, String type,  String username) throws Exception;
    Job findById(Long jobId) throws Exception;
    JobApplication applyForJob(Long jobId, ApplyJobRequestDTO applyJobRequestDTO) throws Exception;

    Page<JobDTO> getJobsByCompany(PageRequest pageRequest, String title, String status, String type, String username) throws Exception;
    void likeJob(Long jobId, String username) throws Exception;
    JobListResponse getFavoriteJobs(User user) throws Exception;
    List<JobDTO> getJobsByCompanyId(Long companyId, String username) throws Exception;
    void approveJob(Long jobId) throws Exception;
    void rejectJob(Long jobId) throws Exception;
    void expiredJob(Long jobId) throws Exception;
    void vipJob(Long jobId) throws Exception;
    Page<JobDTO> searchJobs(PageRequest pageRequest, String keyword, String location, String jobType, String username);
    Page<JobDTO> getAppliedJobs(PageRequest pageRequest, String username) throws Exception;
    int countJobsByCompanyId(String username) throws Exception;
    int countPendingJobsByCompanyId(String username);
    List<JobDTO> getLatestJobs(String username) throws Exception;
    void editJob(Long jobId, JobDTO jobDTO) throws Exception;
    int countJobs() throws Exception;
    int countJobsMonthly(int month, int year) throws Exception;
    List<MonthlyCountResponse> countJobsByMonth(int year) throws Exception;
    void closeJob(Long jobId) throws Exception;
    List<JobDTO> get5LatestJobs() throws Exception;
    int countJobsByStatusIn(List<JobStatus> statuses) throws Exception;
}
