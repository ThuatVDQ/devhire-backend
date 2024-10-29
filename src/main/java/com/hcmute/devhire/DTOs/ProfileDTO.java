package com.hcmute.devhire.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileDTO {
    @JsonProperty("full_name")
    private String fullName;

    private String introduction;
    private String gender;
}
