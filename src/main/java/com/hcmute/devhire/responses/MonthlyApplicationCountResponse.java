package com.hcmute.devhire.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonthlyApplicationCountResponse {
    private Integer month;

    @JsonProperty("application_count")
    private Long applicationCount;
}
