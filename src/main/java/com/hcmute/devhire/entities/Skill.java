package com.hcmute.devhire.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "skill")
@Builder
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "frequency", nullable = false, columnDefinition = "int default 0")
    private int frequency;
//    @ManyToOne
//    @JoinColumn(name = "job_id", referencedColumnName = "id")
//    @JsonBackReference
//    private Job job;
    @OneToMany(mappedBy = "skill")
    @JsonManagedReference
    private List<JobSkill> jobSkills;
}
