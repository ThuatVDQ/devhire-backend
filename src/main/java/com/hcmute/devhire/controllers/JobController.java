package com.hcmute.devhire.controllers;

import com.hcmute.devhire.DTOs.ApplyJobRequestDTO;
import com.hcmute.devhire.DTOs.CVDTO;
import com.hcmute.devhire.DTOs.JobApplicationDTO;
import com.hcmute.devhire.DTOs.JobDTO;
import com.hcmute.devhire.components.FileUtil;
import com.hcmute.devhire.entities.CV;
import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.entities.JobApplication;
import com.hcmute.devhire.responses.JobListResponse;
import com.hcmute.devhire.services.ICVService;
import com.hcmute.devhire.services.IJobService;
import com.hcmute.devhire.services.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jobs")
public class JobController {
    private final IJobService jobService;
    private final ICVService cvService;
    private final FileUtil fileUtil;
    @GetMapping("")
    public ResponseEntity<?> getAllJobs() {
        List<Job> jobs = jobService.getAllJobs();
        return ResponseEntity.ok(jobs);
    }

    @PostMapping("")
    public ResponseEntity<?> createJob(@RequestBody JobDTO jobDTO) {
        Job newJob = jobService.createJob(jobDTO);
        return ResponseEntity.ok(newJob);
    }

    @PostMapping(value = "/{jobId}/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> applyJob(
            @PathVariable("jobId") Long jobId,
            @RequestParam("userId") Long userId,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("No file uploaded");
            }

            if (fileUtil.isFileSizeValid(file)) { // >10mb
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body("File is too large! Maximum size is 10MB");
            }

            if (fileUtil.isImageOrPdfFormatValid(file)) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body("File must be an image or a PDF");
            }

            CVDTO cvDTO = cvService.uploadCV(userId, file);
            CV cv = cvService.createCV(cvDTO);
            ApplyJobRequestDTO applyJobRequestDTO = ApplyJobRequestDTO.builder()
                    .userId(userId)
                    .cvId(cv.getId())
                    .jobId(jobId)
                    .build();

            JobApplication jobApplication = jobService.applyForJob(jobId, applyJobRequestDTO);
            return ResponseEntity.ok().body("Applied successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
