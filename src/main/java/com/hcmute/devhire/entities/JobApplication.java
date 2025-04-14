package com.hcmute.devhire.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.hcmute.devhire.utils.JobApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "job_application")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobApplication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "letter", columnDefinition = "TEXT")
    private String letter;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private JobApplicationStatus status;

    @OneToMany(mappedBy = "jobApplication", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InterviewSchedule> interviewSchedules;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonBackReference
    private User user;

    @ManyToOne
    @JoinColumn(name = "job_id", referencedColumnName = "id")
    @JsonBackReference
    private Job job;

    @ManyToOne
    @JoinColumn(name = "cv_id", nullable = false)
    @JsonBackReference
    private CV cv;
}
