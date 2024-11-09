package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByJobId(Long jobId);
    Page<JobApplication> findByUserId(Long userId, PageRequest pageRequest);
    JobApplication findByJobIdAndUserId(Long jobId, Long userId);
    void deleteByJobIdAndUserId(Long jobId, Long userId);
    void deleteByJobId(Long jobId);
    void deleteByUserId(Long userId);


}
