package com.hcmute.devhire.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonthlyCountResponse {
    private Integer month;

    @JsonProperty("count")
    private Long count;
}
