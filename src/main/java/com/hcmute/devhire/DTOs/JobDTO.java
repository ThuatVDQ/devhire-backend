package com.hcmute.devhire.DTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobDTO {

    private Long id;
    @NotBlank(message = "Title cannot be blank")
    private String title;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    @NotBlank(message = "Salary start cannot be blank")
    @JsonProperty("salary_start")
    private double salaryStart;

    @NotBlank(message = "Salary end cannot be blank")
    @JsonProperty("salary_end")
    private double salaryEnd;

    @NotBlank(message = "Type cannot be blank")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String type;

    @NotBlank(message = "Currency cannot be blank")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String currency;

    @NotBlank(message = "Experience cannot be blank")
    private String experience;

    @NotBlank(message = "Position cannot be blank")
    private String position;

    @NotBlank(message = "Level cannot be blank")
    private String level;

    @NotBlank(message = "Requirement cannot be blank")
    private String requirement;

    @NotBlank(message = "Benefit cannot be blank")
    private String benefit;

    @NotBlank(message = "Deadline cannot be blank")
    private Date deadline;

    @NotBlank(message = "Slots cannot be blank")
    private int slots;
    private int applyNumber;
    private int likeNumber;
    private int views;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String status;

    private CompanyDTO company;
    private CategoryDTO category;

    private List<AddressDTO> addresses;
    private List<SkillDTO> skills;
}
