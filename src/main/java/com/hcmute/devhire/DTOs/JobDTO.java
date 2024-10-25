package com.hcmute.devhire.DTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobDTO {
    @NotNull(message = "Title cannot be null")
    private String title;

    @NotNull(message = "Description cannot be null")
    private String description;

    @NotNull(message = "Salary start cannot be null")
    @JsonProperty("salary_start")
    private double salaryStart;

    @NotNull(message = "Salary end cannot be null")
    @JsonProperty("salary_end")
    private double salaryEnd;

    @NotNull(message = "Type cannot be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String type;

    @NotNull(message = "Currency cannot be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String currency;

    @NotNull(message = "Experience cannot be null")
    private String experience;

    @NotNull(message = "Position cannot be null")
    private String position;

    @NotNull(message = "Level cannot be null")
    private String level;

    @NotNull(message = "Requirement cannot be null")
    private String requirement;

    @NotNull(message = "Benefit cannot be null")
    private String benefit;

    @NotNull(message = "Deadline cannot be null")
    private Date deadline;

    @NotNull(message = "Slots cannot be null")
    private int slots;
    private int applyNumber;
    private int likeNumber;
    private int views;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String status;

    private CompanyDTO company;
    private CategoryDTO category;

    private List<AddressDTO> jobAddresses;
    private List<SkillDTO> jobSkills;
}
