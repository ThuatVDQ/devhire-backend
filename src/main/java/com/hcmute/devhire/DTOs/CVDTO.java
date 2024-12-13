package com.hcmute.devhire.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CVDTO {
    private Long id;

    @JsonProperty("cv_url")
    private String cvUrl;
    private String name;

    @JsonProperty("user_id")
    private Long userId;
}
