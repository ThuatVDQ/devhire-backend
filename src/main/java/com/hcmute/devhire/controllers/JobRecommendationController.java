package com.hcmute.devhire.controllers;
import com.hcmute.devhire.DTOs.JobDTO;
import com.hcmute.devhire.components.JwtUtil;
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
    public ResponseEntity<?> getRecommendedJobs(
            @RequestParam(defaultValue = "5") int topK) {
        try {
            String username = JwtUtil.getAuthenticatedUsername();
            List<JobDTO> jobs = recommendationService.recommendJobsForUser(username, topK);
            return ResponseEntity.ok(jobs);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
