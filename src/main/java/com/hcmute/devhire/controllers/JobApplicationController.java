package com.hcmute.devhire.controllers;

import com.hcmute.devhire.DTOs.CVDTO;
import com.hcmute.devhire.DTOs.JobApplicationDTO;
import com.hcmute.devhire.entities.CV;
import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.exceptions.DataNotFoundException;
import com.hcmute.devhire.services.ICVService;
import com.hcmute.devhire.services.IJobApplicationService;
import com.hcmute.devhire.services.IJobService;
import com.hcmute.devhire.services.IUserService;
import lombok.RequiredArgsConstructor;
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
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/job-application")
public class JobApplicationController {
    private final IUserService userService;
    private final IJobService jobService;
    private final IJobApplicationService jobApplicationService;
    private final ICVService cvService;

    @PostMapping("/apply/{jobId}")
    public ResponseEntity<?> applyJob(
            @PathVariable("jobId") Long jobId,
            @RequestBody Long userId
            ) {
        try {
            User existUser = userService.findById(userId);

            Job existJob = jobService.findById(jobId);

            CV cv = cvService.findByUserId(userId);
            if (cv == null) {
                return ResponseEntity.badRequest().body("User has not uploaded a CV");
            }
            JobApplicationDTO jobApplicationDTO = JobApplicationDTO.builder()
                    .jobId(jobId)
                    .userId(userId)
                    .cvId(cv.getId())
                    .build();
            jobApplicationService.applyJob(jobApplicationDTO);
            return ResponseEntity.ok().body("Applied successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "/apply/upload-cv/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadCV(
            @ModelAttribute("file") MultipartFile file,
            @PathVariable("userId") Long userId
    ) throws Exception {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("No file uploaded");
            }

            // Kiểm tra kích thước file
            if (file.getSize() > 10 * 1024 * 1024) { // >10mb
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body("File is too large! Maximum size is 10MB");
            }

            // Kiểm tra định dạng file
            String contentType = file.getContentType();
            if (contentType == null ||
                    (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body("File must be an image or a PDF");
            }

            // Lưu file và lưu vào database
            String filename = storeFile(file);
            CV cv = cvService.createCV(
                    CVDTO.builder()
                            .cvUrl(filename)
                            .userId(userId)
                            .build()
            );
            return ResponseEntity.ok().body(cv);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }


    }
    private String storeFile(MultipartFile file) throws IOException {
        if (file.getOriginalFilename() == null) {
            throw new IOException("Invalid image format");
        }
        String filename = StringUtils.cleanPath(file.getOriginalFilename());

        String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;
        Path uploadDir = Paths.get("uploads");

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path destination = Paths.get(uploadDir.toString(), uniqueFilename);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFilename;
    }

    @PostMapping(value = "/upload-cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(
            @ModelAttribute("file") MultipartFile file
    ) throws Exception {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("No file uploaded");
            }

            // Kiểm tra kích thước file
            if (file.getSize() > 10 * 1024 * 1024) { // >10mb
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body("File is too large! Maximum size is 10MB");
            }

            // Kiểm tra định dạng file
            String contentType = file.getContentType();
            if (contentType == null ||
                    (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body("File must be an image or a PDF");
            }

            // Lưu file và lưu vào database
            String filename = storeFile(file);

            return ResponseEntity.ok().body(filename);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
