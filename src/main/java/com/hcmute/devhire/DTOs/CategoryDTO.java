package com.hcmute.devhire.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryDTO {
    private Long id;
    private String name;

    @JsonProperty("total_jobs")
    private Long totalJobs;
}
