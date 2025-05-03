package com.hcmute.devhire.services;

import com.hcmute.devhire.entities.Job;

import java.util.List;

public interface IRecommendationService {
    List<Job> recommendJobsForUser(String email, int topK);
}
