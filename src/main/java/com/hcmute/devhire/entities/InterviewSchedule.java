package com.hcmute.devhire.entities;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.hcmute.devhire.utils.InterviewResult;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_schedule")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InterviewSchedule extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_application_id", nullable = false)
    @JsonBackReference
    private JobApplication jobApplication;

    @Column(name = "interview_time", nullable = false)
    private LocalDateTime interviewTime;

    @Column(name = "duration_minutes")
    private int durationMinutes;

    @Column(name = "location", length = 255)
    private String location; // Có thể là địa chỉ thực hoặc link họp online

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, columnDefinition = "varchar(10) default 'WAITING'")
    private InterviewResult result = InterviewResult.WAITING;

    @Column(name = "recruiter_note", columnDefinition = "TEXT")
    private String recruiterNote;

    @Column(name = "email_sent", columnDefinition = "BOOLEAN DEFAULT FALSE", nullable = false)
    private boolean emailSent;
}
