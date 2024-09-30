package com.hcmute.devhire.entities;

import jakarta.persistence.*;
import lombok.*;

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
}
