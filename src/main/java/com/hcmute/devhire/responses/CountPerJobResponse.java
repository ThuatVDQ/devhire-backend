package com.hcmute.devhire.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CountPerJobResponse {
    private String jobTitle;
    private Long count;

    public CountPerJobResponse(String jobTitle, Long count) {
        this.jobTitle = jobTitle;
        this.count = count;
    }
}
