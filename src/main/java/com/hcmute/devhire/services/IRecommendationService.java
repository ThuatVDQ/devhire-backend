package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.JobDTO;
import com.hcmute.devhire.entities.Job;

import java.util.List;

public interface IRecommendationService {
    List<JobDTO> recommendJobsForUser(String email, int topK);
}
