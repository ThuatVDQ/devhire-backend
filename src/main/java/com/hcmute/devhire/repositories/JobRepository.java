package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.responses.MonthlyCountResponse;
import com.hcmute.devhire.utils.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    Page<Job> findByStatusIn(List<JobStatus> statuses, Pageable pageable);
    List<Job> findByStatusIn(List<JobStatus> statuses);
    Page<Job> findAll(Pageable pageable);
    Optional<Job> findById(Long id);
    List<Job> findByCompanyId(Long companyId);
    Page<Job> findByCompanyIdOrderByIdDesc(Long companyId, Pageable pageable);
    Page<Job> findByCompanyId(Long companyId, Pageable pageable);

    @Query("SELECT COUNT(j) FROM Job j WHERE j.company.id = ?1")
    int countJobsByCompany(Long companyId);

    @Query("SELECT COUNT(j) FROM Job j WHERE j.company.id = :companyId AND j.createdAt >= :startDate AND j.status IN :statuses")
    long countJobsByCompanyIdAndCreatedAtAfterAndStatuses(
            @Param("companyId") Long companyId,
            @Param("startDate") LocalDateTime startDate,
            @Param("statuses") Collection<JobStatus> statuses
    );

    @Query("SELECT COUNT(j) FROM Job j WHERE j.company.id = ?1 AND j.status = 'PENDING'")
    int countPendingJobsByCompanyId(Long companyId);

    List<Job> findTop5ByCompanyIdOrderByCreatedAtDesc(Long companyId);

    @Query("SELECT COUNT(j) FROM Job j")
    int countJobs();

    int countJobsByStatusIn(List<JobStatus> statuses);

    @Query("SELECT COUNT(j) FROM Job j WHERE MONTH(j.createdAt) = ?1 AND YEAR(j.createdAt) = ?2")
    int countJobsMonthly(int month, int year);

    @Query("SELECT new com.hcmute.devhire.responses.MonthlyCountResponse(MONTH(j.createdAt), COUNT(j)) " +
            "FROM Job j " +
            "WHERE YEAR(j.createdAt) = :year " +
            "GROUP BY MONTH(j.createdAt) " +
            "ORDER BY MONTH(j.createdAt)")
    List<MonthlyCountResponse> countJobsByMonth(@Param("year") int year);

    List<Job> findTop5ByOrderByCreatedAtDesc();

    List<Job> findByDeadlineBeforeAndStatusIn(LocalDateTime deadline, List<JobStatus> statuses);

    @Query(value = "SELECT DISTINCT j FROM Job j " +
            "JOIN JobSkill js1 ON j.id = js1.job.id " +
            "JOIN JobSkill js2 ON js1.skill.id = js2.skill.id " +
            "WHERE js2.job.id = :jobId " +
            "AND j.id != :jobId " +
            "AND j.status IN :statuses")
    List<Job> getRelatedJobs(@Param("jobId") Long jobId, @Param("statuses") List<JobStatus> statuses);

    @Query("SELECT j FROM Job j WHERE j.category.id IN :categoryIds AND j.status IN :statuses")
    Page<Job> getJobsByCategoryIds(@Param("categoryIds") List<Long> categoryIds, @Param("statuses") List<JobStatus> statuses, Pageable pageable);

    @Query("SELECT COUNT(j) FROM Job j WHERE j.category.id = :categoryId")
    Long countJobsByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT j FROM Job j " +
            "WHERE j.category.id IN (SELECT ja.job.id FROM JobApplication ja WHERE ja.user.id = :userId) " +
            "OR j.id IN (SELECT js.job.id FROM JobSkill js WHERE js.skill.id IN " +
            "(SELECT js2.skill.id FROM JobSkill js2 WHERE js2.job.id IN " +
            "(SELECT ja.job.id FROM JobApplication ja WHERE ja.user.id = :userId))) " +
            "AND j.deadline >= :recentDate " +
            "AND (j.status = 'HOT' OR j.status = 'OPEN')")
    List<Job> findSimilarJobs(@Param("userId") Long userId, @Param("recentDate") LocalDateTime recentDate);

    List<Job> findByHighlightEndTimeBefore(LocalDateTime time);

    List<Job> findByCompanyIdAndStatus(Long companyId, JobStatus status);

    @Query("SELECT COUNT(j) FROM Job j WHERE j.company.id = :companyId AND j.status IN :statuses")
    long countByCompanyIdAndStatuses(@Param("companyId") Long companyId,
                                     @Param("statuses") Collection<JobStatus> statuses);

    @Query(value = """
    SELECT DISTINCT j.id
    FROM job j
    JOIN job_skill js ON js.job_id = j.id
    JOIN skill s ON s.id = js.skill_id
    WHERE LOWER(s.name) LIKE %:keyword%
      AND j.status IN (:statuses)
    """, nativeQuery = true)
    List<Long> findJobIdsBySkillKeywordNative(@Param("keyword") String keyword,
                                              @Param("statuses") List<JobStatus> statuses);
}
