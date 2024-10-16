package com.hcmute.devhire.entities;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_address")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;
}
