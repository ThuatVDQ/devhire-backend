package com.hcmute.devhire.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hcmute.devhire.entities.Job;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyDTO {
    private Long id;

    @JsonProperty("tax_code")
    @NotBlank(message = "Tax code is required")
    private String taxCode;

    @NotBlank(message = "Name is required")
    @Size(min=3, max=200, message = "Name must be between 3 and 200 characters")
    private String name;

    private String logo;
    private String description;
    private int scale;

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Address is required")
    private String address;

    @JsonProperty("web_url")
    private String webUrl;

    @JsonProperty("phone_verified")
    private boolean phoneVerified;

    @JsonProperty("company_status")
    private String companyStatus;

    @JsonProperty("business_license")
    private String businessLicense;

    @JsonProperty("images")
    private List<String> images;
    private double score;
    private String status;
    private UserDTO createBy;
    private List<JobDTO> jobs;
    private int totalJob;
    private int totalReviews;
}
