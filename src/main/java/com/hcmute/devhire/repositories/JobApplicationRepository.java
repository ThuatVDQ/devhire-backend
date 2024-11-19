package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.JobApplication;
import com.hcmute.devhire.responses.CountPerJobResponse;
import com.hcmute.devhire.responses.MonthlyApplicationCountResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByJobId(Long jobId);
    Page<JobApplication> findByUserId(Long userId, PageRequest pageRequest);
    JobApplication findByJobIdAndUserId(Long jobId, Long userId);
    void deleteByJobIdAndUserId(Long jobId, Long userId);
    void deleteByJobId(Long jobId);
    void deleteByUserId(Long userId);

    @Query("SELECT COUNT(ja) FROM JobApplication ja WHERE ja.job.company.id = ?1")
    int countByCompanyId(Long companyId);


    @Query("SELECT new com.hcmute.devhire.responses.CountPerJobResponse(a.job.title, COUNT(a)) " +
            "FROM JobApplication a " +
            "WHERE a.job.company.id = :companyId " +
            "GROUP BY a.job.id, a.job.title")
    List<CountPerJobResponse> countApplicationsPerJobByCompany(@Param("companyId") Long companyId);

    @Query("SELECT new com.hcmute.devhire.responses.MonthlyApplicationCountResponse(MONTH(a.createdAt), COUNT(a)) " +
            "FROM JobApplication a " +
            "WHERE YEAR(a.createdAt) = :year AND a.job.company.id = :companyId " +
            "GROUP BY MONTH(a.createdAt) " +
            "ORDER BY MONTH(a.createdAt)")
    List<MonthlyApplicationCountResponse> countApplicationsByMonthForCompany(@Param("year") int year, @Param("companyId") Long companyId);

    @Query("SELECT new com.hcmute.devhire.responses.CountPerJobResponse(a.job.title, COUNT(a)) " +
            "FROM JobApplication a " +
            "GROUP BY a.job.id, a.job.title")
    List<CountPerJobResponse> countApplicationsPerJob();

    @Query("SELECT new com.hcmute.devhire.responses.MonthlyApplicationCountResponse(MONTH(a.createdAt), COUNT(a)) " +
            "FROM JobApplication a " +
            "WHERE YEAR(a.createdAt) = :year " +
            "GROUP BY MONTH(a.createdAt) " +
            "ORDER BY MONTH(a.createdAt)")
    List<MonthlyApplicationCountResponse> countApplicationsByMonthForCompany(@Param("year") int year);
}
