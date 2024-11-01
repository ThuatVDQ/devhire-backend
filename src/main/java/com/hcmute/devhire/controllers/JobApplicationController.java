package com.hcmute.devhire.controllers;

import com.hcmute.devhire.DTOs.JobApplicationDTO;
import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.entities.JobApplication;
import com.hcmute.devhire.services.IJobApplicationService;
import com.hcmute.devhire.utils.JobApplicationStatus;
import com.hcmute.devhire.utils.JobStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    @GetMapping("/{jobId}/download-cv")
    public ResponseEntity<?> downloadCV(
            @PathVariable("jobId") Long jobId
    ) {
        try {
            List<String> cvPaths = jobApplicationService.getAllCvPathsByJobId(jobId);

            if (cvPaths.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Path zipPath = Files.createTempFile("cvs_for_job", ".zip");
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipPath))) {
                for (String cvPath : cvPaths) {
                    File file = new File("uploads/" + cvPath);
                    if (file.exists()) {
                        zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
                        Files.copy(file.toPath(), zipOutputStream);
                        zipOutputStream.closeEntry();
                    }
                }
            }

            Resource resource = new FileSystemResource(zipPath.toFile());
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=all_cvs_for_job" + ".zip");
            headers.add("Access-Control-Expose-Headers", "Content-Disposition");
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(zipPath.toFile().length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/{jobApplicationId}/seen")
    public ResponseEntity<?> seenJobApplication(
            @PathVariable("jobApplicationId") Long jobApplicationId
    ) {
        try {
            JobApplicationDTO jobApplicationDTO = jobApplicationService.getJobApplication(jobApplicationId);
            if (jobApplicationDTO == null) {
                return ResponseEntity.badRequest().body("Job not found");
            }
            if (!jobApplicationDTO.getStatus().equals(JobApplicationStatus.IN_PROGRESS.name())) {
                return ResponseEntity.badRequest().body("Job application is not in progress");
            }
            jobApplicationService.seenJobApplication(jobApplicationId);
            return ResponseEntity.ok().body("Seen job application");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{jobApplicationId}/reject")
    public ResponseEntity<?> rejectJobApplication(
            @PathVariable("jobApplicationId") Long jobApplicationId
    ) {
        try {
            JobApplicationDTO jobApplicationDTO = jobApplicationService.getJobApplication(jobApplicationId);
            if (jobApplicationDTO == null) {
                return ResponseEntity.badRequest().body("Job not found");
            }
            if (!jobApplicationDTO.getStatus().equals(JobApplicationStatus.SEEN.name())) {
                return ResponseEntity.badRequest().body("Job application is not seen");
            }
            jobApplicationService.rejectJobApplication(jobApplicationId);
            return ResponseEntity.ok().body("Rejected job application");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{jobApplicationId}/accept")
    public ResponseEntity<?> approveJobApplication(
            @PathVariable("jobApplicationId") Long jobApplicationId
    ) {
        try {
            JobApplicationDTO jobApplicationDTO = jobApplicationService.getJobApplication(jobApplicationId);
            if (jobApplicationDTO == null) {
                return ResponseEntity.badRequest().body("Job not found");
            }
            if (!jobApplicationDTO.getStatus().equals(JobApplicationStatus.SEEN.name())) {
                return ResponseEntity.badRequest().body("Job application is not seen");
            }
            jobApplicationService.approveJobApplication(jobApplicationId);
            return ResponseEntity.ok().body("Approved job application");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
