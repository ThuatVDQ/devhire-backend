package com.hcmute.devhire.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_address")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobAddress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_id", referencedColumnName = "id")
    private Job job;

    @Column(name = "country", length = 50)
    private String country;

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "district", length = 50)
    private String district;

    @Column(name = "street", length = 255)
    private String street;
}
