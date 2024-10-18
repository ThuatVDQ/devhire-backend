package com.hcmute.devhire.DTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobDTO {
    private String title;
    private String description;
    @JsonProperty("salary_start")
    private double salaryStart;
    @JsonProperty("salary_end")
    private double salaryEnd;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String type;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String currency;
    private String experience;
    private String position;
    private String level;
    private String requirement;
    private String benefit;
    private Date deadline;
    private int slots;
    private int applyNumber;
    private int likeNumber;
    private int views;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String status;

    private CategoryDTO category;
    private List<AddressDTO> jobAddresses;
    private List<SkillDTO> jobSkills;
}
