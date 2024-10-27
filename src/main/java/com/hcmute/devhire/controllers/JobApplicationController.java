package com.hcmute.devhire.controllers;

import com.hcmute.devhire.DTOs.JobApplicationDTO;
import com.hcmute.devhire.entities.JobApplication;
import com.hcmute.devhire.services.IJobApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/job-application")
public class JobApplicationController {
    private final IJobApplicationService jobApplicationService;

    @GetMapping("/{jobId}")
    public ResponseEntity<?> getJobApplicationByJob(
            @PathVariable("jobId") Long jobId
    ) {
        try {
            List<JobApplicationDTO> jobApplicationDTOS = jobApplicationService.findByJobId(jobId);
            return ResponseEntity.ok(jobApplicationDTOS);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }
}
