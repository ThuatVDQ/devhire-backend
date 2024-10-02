package com.hcmute.devhire.controllers;

import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.services.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jobs")
public class JobController {
    private final JobService jobService;
    @GetMapping("")
    public ResponseEntity<?> getAllJobs() {
        List<Job> jobs = jobService.getAllJobs();
        return ResponseEntity.ok(jobs);
    }
}
