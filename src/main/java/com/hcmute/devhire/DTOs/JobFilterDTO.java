package com.hcmute.devhire.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hcmute.devhire.utils.JobType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobFilterDTO {
    private List<String> cities;
    private List<String> districts;

    @JsonProperty("salary_min")
    private Double salaryMin;

    @JsonProperty("salary_max")
    private Double salaryMax;
    private String currency;
    private List<JobType> types;
    private List<String> levels;
    private List<String> experiences;
    private List<String> positions;

    private List<String> categories;

    @JsonProperty("skills")
    private List<String> skills;

    @JsonProperty("company_name")
    private String companyName;
}
