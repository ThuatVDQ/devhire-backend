package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.utils.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    Page<Job> findByStatusIn(List<JobStatus> statuses, Pageable pageable);
    Page<Job> findAll(Pageable pageable);
    Optional<Job> findById(Long id);
    List<Job> findByCompanyId(Long companyId);

    Page<Job> findByCompanyId(Long companyId, Pageable pageable);

    @Query("SELECT COUNT(j) FROM Job j WHERE j.company.id = ?1")
    int countJobsByCompany(Long companyId);

    @Query("SELECT COUNT(j) FROM Job j WHERE j.company.id = ?1 AND j.status = 'PENDING'")
    int countPendingJobsByCompanyId(Long companyId);

    List<Job> findTop5ByCompanyIdOrderByCreatedAtDesc(Long companyId);

    @Query("SELECT COUNT(j) FROM Job j")
    int countJobs();

    @Query("SELECT COUNT(j) FROM Job j WHERE MONTH(j.createdAt) = ?1 AND YEAR(j.createdAt) = ?2")
    int countJobsMonthly(int month, int year);
}
