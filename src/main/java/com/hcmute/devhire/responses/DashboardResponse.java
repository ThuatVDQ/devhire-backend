package com.hcmute.devhire.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardResponse {
    private Stats users;
    private Stats jobs;
    private Stats companies;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Stats {
        private int count;
        private double growth;
    }
}
