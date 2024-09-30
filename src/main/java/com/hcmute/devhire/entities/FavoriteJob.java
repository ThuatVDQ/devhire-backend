package com.hcmute.devhire.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "favorite_job")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FavoriteJob extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "job_id", referencedColumnName = "id")
    private Job job;
}
