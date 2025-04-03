package com.hcmute.devhire.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.hcmute.devhire.utils.Currency;
import com.hcmute.devhire.utils.JobStatus;
import com.hcmute.devhire.utils.JobType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "job")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Job extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", length = 255)
    private String title;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "slots")
    private int slots;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private JobType type;

    @Column(name = "experience", length = 50)
    private String experience;

    @Column(name = "position", length = 50)
    private String position;

    @Column(name = "level", length = 50)
    private String level;

    @Column(name = "currency", length = 10)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(name = "salary_start")
    private Double salaryStart;

    @Column(name = "salary_end")
    private Double salaryEnd;

    @Lob
    @Column(name = "requirement", columnDefinition = "TEXT")
    private String requirement;

    @Lob
    @Column(name = "benefit", columnDefinition = "TEXT")
    private String benefit;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "apply_number")
    private int applyNumber;

    @Column(name = "like_number")
    private int likeNumber;

    @Column(name = "views")
    private int views;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private JobStatus status;

    @ManyToOne
    @JoinColumn(name = "company_id", referencedColumnName = "id")
    @JsonBackReference
    private Company company;
//
//    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JsonManagedReference
//    private List<Address> addresses;
//
//    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JsonManagedReference
//    private List<Skill> skills;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<JobApplication> jobApplications;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<JobAddress> jobAddresses;
    
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<JobSkill> jobSkills;

}
