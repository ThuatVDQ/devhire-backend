package com.hcmute.devhire.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "cv")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CV extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cv_url", length = 255)
    private String cvUrl;

    @Column(name = "name", length = 255)
    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @JsonBackReference
    private User user;

    @OneToMany(mappedBy = "cv", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<JobApplication> jobApplications;
}
