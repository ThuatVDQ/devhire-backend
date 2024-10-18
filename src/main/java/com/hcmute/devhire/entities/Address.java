package com.hcmute.devhire.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "address")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne
//    @JoinColumn(name = "job_id", referencedColumnName = "id")
//    @JsonBackReference
//    private Job job;
    @ManyToMany(mappedBy = "addresses", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Job> jobs;

    @Column(name = "country", length = 50)
    private String country;

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "district", length = 50)
    private String district;

    @Column(name = "street", length = 255)
    private String street;
}