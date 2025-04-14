package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.InterviewSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface InterviewScheduleRepository extends JpaRepository<InterviewSchedule, Long> {
    @Query("""
        SELECT COUNT(i) > 0\s
        FROM InterviewSchedule i\s
        WHERE\s
            i.jobApplication.user.id = :userId\s
            AND i.id != :excludeId\s
            AND (
                (i.interviewTime BETWEEN :startTime AND :endTime)\s
                OR\s
                (FUNCTION('DATE_ADD', i.interviewTime, i.durationMinutes * 60, 'SECOND') BETWEEN :startTime AND :endTime)
            )
   \s""")
    boolean existsByUserAndTimeRange(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeId") Long excludeId
    );
}