package com.hcmute.devhire.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
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

    @OneToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<CompanyImage> companyImages;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<CompanyReview> companyReviews;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Job> jobs;

}
