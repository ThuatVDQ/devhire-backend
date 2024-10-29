package com.hcmute.devhire.controllers;

import com.hcmute.devhire.DTOs.JobApplicationDTO;
import com.hcmute.devhire.entities.JobApplication;
import com.hcmute.devhire.services.IJobApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

            Path zipPath = Files.createTempFile("cvs_for_job_" + jobId, ".zip");
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
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=all_cvs_for_job_" + jobId + ".zip");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(zipPath.toFile().length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
