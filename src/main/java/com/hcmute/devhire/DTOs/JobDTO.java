package com.hcmute.devhire.DTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
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

    @NotNull(message = "Salary start cannot be blank")
    @Min(value = 0, message = "Salary start must be greater than 0")
    @JsonProperty("salary_start")
    private Double salaryStart;

    @NotNull(message = "Salary start cannot be blank")
    @JsonProperty("salary_end")
    private Double salaryEnd;

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

    @NotNull(message = "Deadline cannot be blank")
    private LocalDateTime deadline;

    @NotNull(message = "Slots cannot be blank")
    private int slots;

    @JsonProperty("apply_number")
    private int applyNumber;

    @JsonProperty("updated_at")
    private  LocalDateTime updatedAt;

    @JsonProperty("created_at")
    private  LocalDateTime createdAt;

    @JsonProperty("like_number")
    private int likeNumber;
    private int views;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String status;

    private CompanyDTO company;
    private CategoryDTO category;

    private List<AddressDTO> addresses;
    private List<SkillDTO> skills;

    @JsonProperty("is_favorite")
    private boolean isFavorite;

    @JsonProperty("is_close")
    private boolean isClose;

    @JsonProperty("apply_status")
    private String applyStatus;

    @JsonProperty("cv_url")
    private String cvUrl;

    @JsonProperty("is_highlight")
    private boolean isHighlight;

    @JsonProperty("date_applied")
    private LocalDateTime dateApplied;
}
