package com.hcmute.devhire.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "Company")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Company extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tax_code", length = 50)
    private String taxCode;

    @Column(name="name", length = 255)
    private String name;

    @Column(name="logo", length = 255)
    private String logo;

    @Column(name="description")
    private String description;

    @Column(name="scale")
    private int scale;

    @Column(name="email", length = 255)
    private String email;

    @Column(name="phone", length = 20)
    private String phone;

    @Column(name="address", length = 255)
    private String address;

    @Column(name="web_url", length = 255)
    private String webUrl;

    @Column(name="status", length = 20)
    private String status;

    @Column(name="created_by")
    private Long createdBy;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Job> jobs;

    @OneToOne
    private User user;
}
