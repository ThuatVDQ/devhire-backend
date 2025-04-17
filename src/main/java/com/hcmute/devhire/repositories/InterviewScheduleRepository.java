package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.InterviewSchedule;
import com.hcmute.devhire.utils.JobApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface InterviewScheduleRepository extends JpaRepository<InterviewSchedule, Long> {
    @Query(value = """
    SELECT COUNT(i.id) > 0
    FROM interview_schedule i
    JOIN job_application ja ON ja.id = i.job_application_id
    WHERE ja.user_id = :userId
    AND i.id != :excludeId
    AND (
        i.interview_time < :endTime
        AND DATE_ADD(i.interview_time, INTERVAL i.duration_minutes MINUTE) > :startTime
    )
    """, nativeQuery = true)
    int existsByUserAndTimeRange(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeId") Long excludeId
    );

    @Query("SELECT i FROM InterviewSchedule i WHERE i.jobApplication.status = :status")
    Page<InterviewSchedule> findByJobApplicationStatus(
            @Param("status") JobApplicationStatus status,
            PageRequest pageRequest
    );

    @Query("SELECT i FROM InterviewSchedule i " +
            "JOIN i.jobApplication ja " +
            "JOIN ja.job j " +
            "JOIN j.company c " +
            "WHERE c.id = :companyId")
    Page<InterviewSchedule> findAllByCompanyId(@Param("companyId") Long companyId, PageRequest pageRequest);

    @Query("SELECT i FROM InterviewSchedule i " +
            "JOIN i.jobApplication ja " +
            "JOIN ja.job j " +
            "JOIN j.company c " +
            "WHERE c.id = :companyId AND ja.status = :status")
    Page<InterviewSchedule> findAllByCompanyIdAndStatus(@Param("companyId") Long companyId,
                                                        @Param("status") JobApplicationStatus status,
                                                        PageRequest pageRequest);
}