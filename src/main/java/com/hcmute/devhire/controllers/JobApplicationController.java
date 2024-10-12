package com.hcmute.devhire.controllers;

import com.hcmute.devhire.DTOs.JobApplicationDTO;
import com.hcmute.devhire.services.IJobApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/job-application")
public class JobApplicationController {
    private final IJobApplicationService jobApplicationService;

    @PostMapping("/apply/{jobId}")
    public ResponseEntity<?> applyJob(
            @PathVariable long jobId,
            JobApplicationDTO jobApplicationDTO
            ) {

        return ResponseEntity.ok("Id: " + jobId);
    }
}
