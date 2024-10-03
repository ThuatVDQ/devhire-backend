package com.hcmute.devhire.responses;

import com.hcmute.devhire.entities.Job;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class JobListResponse {
    private List<Job> jobs;
    private int totalPages;
}
