package com.hcmute.devhire.controllers;

import com.hcmute.devhire.DTOs.*;
import com.hcmute.devhire.components.FileUtil;
import com.hcmute.devhire.entities.*;
import com.hcmute.devhire.responses.JobListResponse;
import com.hcmute.devhire.services.*;
import com.hcmute.devhire.utils.JobStatus;
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
    private final IEmailService emailService;
    private final NotificationService notificationService;

    @GetMapping("")
    public ResponseEntity<?> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        if (page < 0 || limit <= 0) {
            return ResponseEntity.badRequest().body("Page must be >= 0 and limit must be > 0.");
        }
        try {
            String username = getAuthenticatedUsername();

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
        String username = getAuthenticatedUsername();
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
                        .id(job.getCompany().getId())
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
                    .category(CategoryDTO.builder().name(job.getCategory().getName()).id(job.getCategory().getId()).build())
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
            notificationService.sendNotificationToAdmin(newJob.getCompany().getName() + " has created a new job");
            return ResponseEntity.ok(newJob);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping(value = "/{jobId}/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> applyJob(
            @PathVariable("jobId") Long jobId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("letter") String letter
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
            String username = getAuthenticatedUsername();
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }
            UserDTO userDTO = userService.findByUsername(username);

            Optional<JobApplication> existingApplication = jobApplicationService.findApplicationByJobIdAndUserId(jobId, userDTO.getId());

            CVDTO cvDTO = cvService.uploadCV(userDTO.getId(), file);
            CV cv = cvService.createCV(cvDTO);
            if (existingApplication.isPresent()) {
                JobApplication jobApplication = existingApplication.get();
                jobApplication.setCv(cv);
                jobApplication.setLetter(letter == null ? "" : letter);
                jobApplicationService.updateJobApplication(jobApplication);
                Job job = jobService.findById(jobId);
                notificationService.createAndSendNotification("New CV updated for job " + job.getTitle(), job.getCompany().getCreatedBy().getUsername());
                notificationService.sendNotificationToAdmin("User " + userDTO.getFullName() + " has updated CV for job " + job.getTitle());
                return ResponseEntity.ok().body("Updated CV for existing application");
            } else {
                ApplyJobRequestDTO applyJobRequestDTO = ApplyJobRequestDTO.builder()
                        .userId(userDTO.getId())
                        .cvId(cv.getId())
                        .jobId(jobId)
                        .letter(letter == null ? "" : letter)
                        .build();
                jobService.applyForJob(jobId, applyJobRequestDTO);
                sendNewApplicationNotification(jobId, letter, userDTO);
                Job job = jobService.findById(jobId);
                notificationService.sendNotificationToAdmin("User " + userDTO.getFullName() + " has applied for job " + job.getTitle());
                return ResponseEntity.ok().body("Applied successfully");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public void sendNewApplicationNotification(Long jobId, String letter, UserDTO applicant) throws Exception {
        Job job = jobService.findById(jobId);
        String recruiterEmail = job.getCompany().getCreatedBy().getEmail();
        String recruiterName = job.getCompany().getName();

        notificationService.createAndSendNotification("New CV submitted for job " + job.getTitle(), job.getCompany().getCreatedBy().getUsername());

        String subject = "New CV Submitted for " + job.getTitle();

        String content = String.format(
                """
                <p>Hello %s,</p>
                <p>A new CV has been submitted by %s for the position %s.</p>
                <p><strong>Cover Letter:</strong></p>
                <p>%s</p>
                <p>Please review the application in the system.</p>
                <p>Best regards,<br>DevHire Team</p>
                """,
                recruiterName, applicant.getFullName(), job.getTitle(), letter
        );

        emailService.sendEmail(recruiterEmail, subject, content);
    }


    @GetMapping("/company")
    public ResponseEntity<?> getJobsByRecruiterCompany(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {

        if (page < 0 || limit <= 0) {
            return ResponseEntity.badRequest().body("Page must be >= 0 and limit must be > 0.");
        }

        String username = getAuthenticatedUsername();
        if (username == null) {
            return ResponseEntity.badRequest().body("User is not authenticated");
        }

        try {
            PageRequest pageRequest = PageRequest.of(
                    page, limit,
                    Sort.by("id").descending()
            );

            Page<JobDTO> jobs = jobService.getJobsByCompany(pageRequest, keyword, status, type, username);
            JobListResponse response = JobListResponse.builder()
                    .jobs(jobs.getContent())
                    .currentPage(page)
                    .pageSize(limit)
                    .totalPages(jobs.getTotalPages())
                    .totalElements(jobs.getTotalElements())
                    .build();
            if (jobs.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No jobs found for the user");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<?> getJobsByCompanyId(
            @PathVariable("companyId") Long companyId
    ) {
        try {
            String username = getAuthenticatedUsername();
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
            String username = getAuthenticatedUsername();
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }

            jobService.likeJob(jobId, username);
            return ResponseEntity.ok().body("Liked job successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/{jobId}/approve")
    public ResponseEntity<?> approveJob(
            @PathVariable("jobId") Long jobId
    ) {
        try {
            Job job = jobService.findById(jobId);
            if (job == null) {
                return ResponseEntity.badRequest().body("Job not found");
            }
            if (job.getStatus() != JobStatus.PENDING) {
                return ResponseEntity.badRequest().body("Job is not pending");
            }
            jobService.approveJob(jobId);
            return ResponseEntity.ok().body("Approved job successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{jobId}/close")
    public ResponseEntity<?> closeJob(
            @PathVariable("jobId") Long jobId
    ) {
        try {
            Job job = jobService.findById(jobId);
            if (job == null) {
                return ResponseEntity.badRequest().body("Job not found");
            }
            if (job.getStatus() != JobStatus.OPEN && job.getStatus() != JobStatus.HOT) {
                return ResponseEntity.badRequest().body("Job is not open");
            }
            jobService.closeJob(jobId);
            return ResponseEntity.ok().body("Closed job successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/closeJobs")
    public ResponseEntity<?> closeJobs(
            @RequestBody List<Long> job_ids
    ) {
        try {
            int count =0;
            for (Long jobId : job_ids) {
                Job job = jobService.findById(jobId);
                if (job == null) {
                    return ResponseEntity.badRequest().body("Job not found");
                }
                if (job.getStatus() != JobStatus.OPEN && job.getStatus() != JobStatus.HOT) {
                    count++;
                    continue;
                }
                jobService.closeJob(jobId);
            }
            if (count == 0) {
                return ResponseEntity.ok().body("Closed jobs successfully");
            } else {
                return ResponseEntity.ok().body("There are " + count + " jobs that are not open");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/approveJobs")
    public ResponseEntity<?> approveJobs(
            @RequestBody List<Long> job_ids
    ) {
        try {
            int count = 0;
            for (Long jobId : job_ids) {
                Job job = jobService.findById(jobId);
                if (job == null) {
                    return ResponseEntity.badRequest().body("Job not found");
                }
                if (job.getStatus() != JobStatus.PENDING) {
                    count++;
                    continue;
                }
                jobService.approveJob(jobId);
            }
            if (count == 0) {
                return ResponseEntity.ok().body("Approved jobs successfully");
            } else {
                return ResponseEntity.ok().body("There are " + count + " jobs that are not pending");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/rejectJobs")
    public ResponseEntity<?> rejectJobs(
            @RequestBody List<Long> job_ids
    ) {
        try {
            int count = 0;
            for (Long jobId : job_ids) {
                Job job = jobService.findById(jobId);
                if (job == null) {
                    return ResponseEntity.badRequest().body("Job not found");
                }
                if (job.getStatus() != JobStatus.PENDING) {
                    count++;
                    continue;
                }
                jobService.rejectJob(jobId);
            }
            if (count == 0) {
                return ResponseEntity.ok().body("Rejected jobs successfully");
            } else {
                return ResponseEntity.ok().body("There are " + count + " jobs that are not pending");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{jobId}/reject")
    public ResponseEntity<?> rejectJob(
            @PathVariable("jobId") Long jobId
    ) {
        try {
            Job job = jobService.findById(jobId);
            if (job == null) {
                return ResponseEntity.badRequest().body("Job not found");
            }
            if (job.getStatus() != JobStatus.PENDING) {
                return ResponseEntity.badRequest().body("Job is not pending");
            }
            jobService.rejectJob(jobId);
            return ResponseEntity.ok().body("Rejected job successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/{jobId}/expire")
    public ResponseEntity<?> expireJob(
            @PathVariable("jobId") Long jobId
    ) {
        try {
            Job job = jobService.findById(jobId);
            if (job == null) {
                return ResponseEntity.badRequest().body("Job not found");
            }
            if (job.getStatus() != JobStatus.OPEN) {
                return ResponseEntity.badRequest().body("Job is not open");
            }
            jobService.expiredJob(jobId);
            return ResponseEntity.ok().body("Expired job successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String jobType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        if (page < 0 || limit <= 0) {
            return ResponseEntity.badRequest().body("Page must be >= 0 and limit must be > 0.");
        }
        String username = getAuthenticatedUsername();
        try {

            PageRequest pageRequest = PageRequest.of(
                    page, limit,
                    Sort.by("id").ascending()
            );
            Page<JobDTO> jobDTOPage = jobService.searchJobs(pageRequest, keyword, location, jobType, username);
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

    @GetMapping("/applied")
    public ResponseEntity<?> getAppliedJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        if (page < 0 || limit <= 0) {
            return ResponseEntity.badRequest().body("Page must be >= 0 and limit must be > 0.");
        }

        String username = getAuthenticatedUsername();
        if (username == null) {
            return ResponseEntity.badRequest().body("User is not authenticated");
        }

        try {
            PageRequest pageRequest = PageRequest.of(
                    page, limit,
                    Sort.by("id").ascending()
            );

            Page<JobDTO> jobApplied = jobService.getAppliedJobs(pageRequest, username);
            JobListResponse response = JobListResponse.builder()
                    .jobs(jobApplied.getContent())
                    .currentPage(page)
                    .pageSize(limit)
                    .totalPages(jobApplied.getTotalPages())
                    .totalElements(jobApplied.getTotalElements())
                    .build();
            if (jobApplied.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No jobs found for the user");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/total-posted")
    public ResponseEntity<?> getTotalPostedJobs() {
        String username = getAuthenticatedUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }

        try {
            int totalJobs = jobService.countJobsByCompanyId(username);
            return ResponseEntity.ok(totalJobs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/total-pending")
    public ResponseEntity<?> getTotalPendingJobs() {
        String username = getAuthenticatedUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }

        try {
            int totalJobs = jobService.countPendingJobsByCompanyId(username);
            return ResponseEntity.ok(totalJobs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<?> getLatestJobsByCompany() {
        String username = getAuthenticatedUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }
        try {
            List<JobDTO> latestJobs = jobService.getLatestJobs(username);
            return ResponseEntity.ok(latestJobs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/{jobId}/edit")
    public ResponseEntity<?> editJob(
            @PathVariable("jobId") Long jobId,
            @Valid @RequestBody JobDTO jobDTO,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            List<String> errorMessage = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
            return ResponseEntity.badRequest().body(errorMessage);
        }

        try {
            Job job = jobService.findById(jobId);
            if (job == null) {
                return ResponseEntity.badRequest().body("Job not found");
            }

            String username = getAuthenticatedUsername();

            if (username == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not retrieve authenticated username");
            }

            if (!job.getCompany().getCreatedBy().getUsername().equals(username)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not authorized to edit this job");
            }
            if (job.getStatus() == JobStatus.OPEN || job.getStatus() == JobStatus.HOT) {
                return ResponseEntity.badRequest().body("Cannot edit job that is open or hot");
            }
            jobService.editJob(jobId, jobDTO);
            notificationService.sendNotificationToAdmin("Company: " + job.getCompany().getName() + " has edited a job: " + job.getTitle());
            return ResponseEntity.ok().body("Job updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = null;

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        }

        return username;
    }
}
