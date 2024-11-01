package com.hcmute.devhire.controllers;

import com.hcmute.devhire.DTOs.*;
import com.hcmute.devhire.components.FileUtil;
import com.hcmute.devhire.entities.*;
import com.hcmute.devhire.responses.CompanyListResponse;
import com.hcmute.devhire.responses.JobListResponse;
import com.hcmute.devhire.services.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.validation.FieldError;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jobs")
public class JobController {
    private final IJobService jobService;
    private final ICVService cvService;
    private final IUserService userService;
    private final FileUtil fileUtil;
    private final IJobApplicationService jobApplicationService;
    private final FavoriteJobService favoriteJobService;

    @GetMapping("")
    public ResponseEntity<?> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        if (page < 0 || limit <= 0) {
            return ResponseEntity.badRequest().body("Page must be >= 0 and limit must be > 0.");
        }
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = null;
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
                username = userDetails.getUsername();
            }

            PageRequest pageRequest = PageRequest.of(
                    page, limit,
                    Sort.by("id").ascending()
            );
            Page<JobDTO> jobDTOPage = jobService.getAllJobs(pageRequest, username);
            JobListResponse response = JobListResponse.builder()
                    .jobs(jobDTOPage.getContent())
                    .currentPage(page)
                    .pageSize(limit)
                    .totalPages(jobDTOPage.getTotalPages())
                    .totalElements(jobDTOPage.getTotalElements())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/{jobId}")
    public ResponseEntity<?> getJob(
            @PathVariable("jobId") Long jobId
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        }
        try {
            boolean isLiked = false;
            String applyStatus = null;
            Job job = jobService.findById(jobId);
            if (username != null) {
                User user = userService.findByUserName(username);
                FavoriteJob favoriteJob = favoriteJobService.findByUserAndJob(user, job);
                if (favoriteJob != null) {
                    isLiked = true;
                }
                Optional<JobApplication> jobApplications = Optional.ofNullable(jobApplicationService.findByJobIdAndUserId(job.getId(), user.getId()));
                if (jobApplications.isPresent()) {
                    applyStatus = jobApplications.get().getStatus().name();
                }
            }
            List<AddressDTO> addressDTOs = job.getJobAddresses().stream()
                    .map(jobAddress -> AddressDTO.builder()
                            .city(jobAddress.getAddress().getCity())
                            .district(jobAddress.getAddress().getDistrict())
                            .street(jobAddress.getAddress().getStreet())
                            .build())
                    .toList();

            List<SkillDTO> skillDTOs = job.getJobSkills().stream()
                    .map(jobSkill -> SkillDTO.builder()
                            .name(jobSkill.getSkill().getName())
                            .build())
                    .toList();
            CompanyDTO companyDTO = null;
            if (job.getCompany() == null) {
                return ResponseEntity.badRequest().body("Company not found");
            } else {
               companyDTO = CompanyDTO.builder()
                        .name(job.getCompany().getName() == null ? "" : job.getCompany().getName())
                        .logo(job.getCompany().getLogo() == null ? "" : job.getCompany().getLogo())
                        .address(job.getCompany().getAddress() == null ? "" : job.getCompany().getAddress())
                        .webUrl(job.getCompany().getWebUrl() == null ? "" : job.getCompany().getWebUrl())
                        .build();
            }
            JobDTO jobDTO = JobDTO.builder()
                    .title(job.getTitle())
                    .description(job.getDescription())
                    .salaryStart(job.getSalaryStart())
                    .salaryEnd(job.getSalaryEnd())
                    .type(job.getType().name())
                    .currency(job.getCurrency().name())
                    .experience(job.getExperience())
                    .position(job.getPosition())
                    .level(job.getLevel())
                    .requirement(job.getRequirement())
                    .benefit(job.getBenefit())
                    .deadline(job.getDeadline())
                    .slots(job.getSlots())
                    .status(job.getStatus().name())
                    .addresses(addressDTOs)
                    .skills(skillDTOs)
                    .company(companyDTO)
                    .isFavorite(isLiked)
                    .applyStatus(applyStatus)
                    .build();
            return ResponseEntity.ok(jobDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("")
    public ResponseEntity<?> createJob(@Valid @RequestBody JobDTO jobDTO, BindingResult result) throws Exception {
        if(result.hasErrors()) {
            List<String> errorMessage = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
            return ResponseEntity.badRequest().body(errorMessage);
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = null;

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }

        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        }

        if (username == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not retrieve authenticated username");
        }

        try {
            Job newJob = jobService.createJob(jobDTO, username);
            return ResponseEntity.ok(newJob);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping(value = "/{jobId}/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> applyJob(
            @PathVariable("jobId") Long jobId,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("No file uploaded");
            }

            if (!fileUtil.isFileSizeValid(file)) { // >10mb
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body("File is too large! Maximum size is 10MB");
            }

            if (!fileUtil.isImageOrPdfFormatValid(file)) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body("File must be an image or a PDF");
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = null;
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
                username = userDetails.getUsername();
            }
            UserDTO userDTO = userService.findByUsername(username);

            Optional<JobApplication> existingApplication = jobApplicationService.findApplicationByJobIdAndUserId(jobId, userDTO.getId());

            CVDTO cvDTO = cvService.uploadCV(userDTO.getId(), file);
            CV cv = cvService.createCV(cvDTO);
            if (existingApplication.isPresent()) {
                JobApplication jobApplication = existingApplication.get();
                jobApplication.setCv(cv);
                jobApplicationService.updateJobApplication(jobApplication);
                return ResponseEntity.ok().body("Updated CV for existing application");
            } else {
                ApplyJobRequestDTO applyJobRequestDTO = ApplyJobRequestDTO.builder()
                        .userId(userDTO.getId())
                        .cvId(cv.getId())
                        .jobId(jobId)
                        .build();
                jobService.applyForJob(jobId, applyJobRequestDTO);
                return ResponseEntity.ok().body("Applied successfully");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/company")
    public ResponseEntity<?> getJobsByRecruiterCompany() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = null;

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }

        try {

            List<JobDTO> jobDTOS = jobService.getJobsByCompany(username);

            if (jobDTOS.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No jobs found for the recruiter's company");
            }
            return ResponseEntity.ok(jobDTOS);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<?> getJobsByCompanyId(
            @PathVariable("companyId") Long companyId
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = null;

            if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
                username = userDetails.getUsername();
            }
            List<JobDTO> jobDTO = jobService.getJobsByCompanyId(companyId, username);

            if (jobDTO.isEmpty()) {
                return ResponseEntity.badRequest().body("No jobs found for the company");
            }
            return ResponseEntity.ok(jobDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/{jobId}/like")
    public ResponseEntity<?> likeJob(
            @PathVariable("jobId") Long jobId
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = null;

            if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
                username = userDetails.getUsername();
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }

            jobService.likeJob(jobId, username);
            return ResponseEntity.ok().body("Liked job successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
