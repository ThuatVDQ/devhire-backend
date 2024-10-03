package com.hcmute.devhire.controllers;

import com.hcmute.devhire.DTOs.JobDTO;
import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.responses.JobListResponse;
import com.hcmute.devhire.services.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jobs")
public class JobController {
    private final JobService jobService;

    @GetMapping("")
    public ResponseEntity<?> getAllJobs(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int limit) {
        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by("id").ascending());
        Page<Job> jobPage = jobService.getAllJobs(pageRequest);
        int totalPages = jobPage.getTotalPages();
        List<Job> jobs = jobPage.getContent();
        return ResponseEntity.ok(Collections.singletonList(JobListResponse.builder().jobs(jobs).totalPages(totalPages).build()));
    }

    @PostMapping("")
    public ResponseEntity<?> createJob(@RequestBody JobDTO jobDTO) {
        Job newJob = jobService.createJob(jobDTO);
        return ResponseEntity.ok(newJob);
    }
}
