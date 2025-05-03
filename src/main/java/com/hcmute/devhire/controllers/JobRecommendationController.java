package com.hcmute.devhire.controllers;
import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.services.IRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class JobRecommendationController {
    private final IRecommendationService recommendationService;

    @GetMapping("")
    public ResponseEntity<List<Job>> getRecommendedJobs(
            @RequestParam String email,
            @RequestParam(defaultValue = "5") int topK) {
        try {
            List<Job> jobs = recommendationService.recommendJobsForUser(email, topK);
            return ResponseEntity.ok(jobs);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
