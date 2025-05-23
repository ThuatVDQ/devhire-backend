package com.hcmute.devhire.controllers;

import com.hcmute.devhire.DTOs.CVDTO;
import com.hcmute.devhire.components.FileUtil;
import com.hcmute.devhire.components.JwtUtil;
import com.hcmute.devhire.entities.CV;
import com.hcmute.devhire.entities.Skill;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.services.CVService;
import com.hcmute.devhire.services.ICVService;
import com.hcmute.devhire.services.ICVSkillExtractorService;
import com.hcmute.devhire.services.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cv")
public class CVController {
    private final ICVService cvService;
    private final FileUtil fileUtil;
    private final IUserService userService;
    private final ICVSkillExtractorService cvSkillExtractorService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadCV(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name
    ) throws Exception {
        try {
            String username = JwtUtil.getAuthenticatedUsername();

            if (username == null) {
                return ResponseEntity.badRequest().body("User not found");
            }

            User user = userService.findByUserName(username);
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("No file uploaded");
            }

            // Kiểm tra kích thước file
            if (!fileUtil.isFileSizeValid(file)) { // >10mb
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body("File is too large! Maximum size is 10MB");
            }

            // Kiểm tra định dạng file
            if (!fileUtil.isImageOrPdfFormatValid(file)) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body("File must be an image or a PDF");
            }
            CVDTO cvDTO = cvService.uploadCV(user.getId(), name, file);
            CV cv = cvService.createCV(cvDTO);

            return ResponseEntity.ok().body(cv);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{cvId}")
    public ResponseEntity<?> getCVById(
            @PathVariable("cvId") Long cvId
    ) {
        try {
            CVDTO cvDTO = cvService.getById(cvId);
            if (cvDTO == null) {
                return ResponseEntity.badRequest().body("CV not found");
            }
            return ResponseEntity.ok(cvDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/{cvId}/download")
    public ResponseEntity<?> downloadCV(
            @PathVariable("cvId") Long cvId
    ) {
        try {
            CVDTO cvDTO = cvService.getById(cvId);
            if (cvDTO == null) {
                return ResponseEntity.badRequest().body("CV not found");
            }
            File file = new File("uploads/" + cvDTO.getCvUrl());
            if (!file.exists()) {
                return ResponseEntity.badRequest().body("CV not found");
            }
            Resource resource = new FileSystemResource(file);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());
            headers.add("Access-Control-Expose-Headers", "Content-Disposition"); // Thêm cấu hình này

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<?> getLatestCV() {
        try {
            String username = JwtUtil.getAuthenticatedUsername();
            if (username == null) {
                return ResponseEntity.badRequest().body("User not found");
            }
            User user = userService.findByUserName(username);
            List<CV> cvs = cvService.findByUserId(user.getId());

            if (cvs.isEmpty()) {
                return ResponseEntity.badRequest().body("CV not found");
            }

            return ResponseEntity.ok(CVDTO.builder()
                    .id(cvs.getLast().getId())
                    .cvUrl(cvs.getLast().getCvUrl())
                    .name(cvs.getLast().getName())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllCVs() {
        try {
            String username = JwtUtil.getAuthenticatedUsername();
            if (username == null) {
                return ResponseEntity.badRequest().body("User not found");
            }
            User user = userService.findByUserName(username);
            List<CV> cvs = cvService.findByUserId(user.getId());

            if (cvs.isEmpty()) {
                return ResponseEntity.badRequest().body("CV not found");
            }

            return ResponseEntity.ok(cvs.stream().map(cv -> CVDTO.builder()
                    .id(cv.getId())
                    .cvUrl(cv.getCvUrl())
                    .name(cv.getName())
                    .build()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/{cvId}/skills")
    public ResponseEntity<List<Skill>> extractSkills(@PathVariable Long cvId) throws Exception {
        return ResponseEntity.ok(cvSkillExtractorService.extractSkillsFromCV(cvId));
    }
}
