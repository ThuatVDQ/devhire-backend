package com.hcmute.devhire.entities;

import com.hcmute.devhire.Utils.JobApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private JobApplicationStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "job_id", referencedColumnName = "id")
    private Job job;

    @OneToOne
    private CV cv;
}
