package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.*;
import com.hcmute.devhire.entities.*;
import com.hcmute.devhire.repositories.*;
import com.hcmute.devhire.repositories.specification.JobSpecifications;
import com.hcmute.devhire.responses.JobListResponse;
import com.hcmute.devhire.responses.MonthlyCountResponse;
import com.hcmute.devhire.utils.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService implements IJobService {
    private final JobRepository jobRepository;
    private final ICategoryService categoryService;
    private final JobAddressRepository jobAddressRepository;
    private final JobSkillRepository jobSkillRepository;
    private final IUserService userService;
    private final JobApplicationRepository jobApplicationRepository;
    private final IAddressService addressService;
    private final ISkillService skillService;
    private final ICompanyService companyService;
    private final CVRepository cvRepository;
    private final IFavoriteJobService favoriteJobService;
    private final FavoriteJobRepository favoriteJobRepository;
    private final IJobApplicationService jobApplicationService;
    private final INotificationService notificationService;

    @Override
    @Transactional
    public Job createJob(JobDTO jobDTO, String username) throws Exception {
        try {
            JobType jobType = EnumUtil.getEnumFromString(JobType.class, jobDTO.getType());
            Currency currency = EnumUtil.getEnumFromString(Currency.class, jobDTO.getCurrency());
            JobStatus status = EnumUtil.getEnumFromString(JobStatus.class, "PENDING");

            Category category = categoryService.findById(jobDTO.getCategory().getId());
            Company company = companyService.findByUser(username);
            if (company == null) {
                throw new Exception("Company not found");
            }

            Job newJob = Job.builder()
                    .title(jobDTO.getTitle())
                    .description(jobDTO.getDescription())
                    .salaryStart(jobDTO.getSalaryStart())
                    .salaryEnd(jobDTO.getSalaryEnd())
                    .type(jobType)
                    .currency(currency)
                    .experience(jobDTO.getExperience())
                    .position(jobDTO.getPosition())
                    .level(jobDTO.getLevel())
                    .requirement(jobDTO.getRequirement())
                    .benefit(jobDTO.getBenefit())
                    .deadline(jobDTO.getDeadline())
                    .slots(jobDTO.getSlots())
                    .status(status)
                    .category(category)
                    .company(company)
                    .build();
            Job savedJob = jobRepository.save(newJob);

            // Handle addresses
            if (jobDTO.getAddresses() != null) {
                jobAddressRepository.deleteAllByJobId(savedJob.getId());
                List<Address> addresses = jobDTO.getAddresses().stream()
                        .map(addressService::createAddress).toList();
                List<JobAddress> jobAddresses = addresses.stream()
                        .map(address -> JobAddress.builder()
                                .job(savedJob)
                                .address(address)
                                .build())
                        .toList();
                jobAddressRepository.saveAll(jobAddresses);
            }

            // Handle skills
            if (jobDTO.getSkills() != null) {
                jobSkillRepository.deleteAllByJobId(savedJob.getId());
                List<Skill> skills = jobDTO.getSkills().stream()
                        .map(skillService::createSkill).toList();
                List<JobSkill> jobSkills = skills.stream()
                        .map(skill -> JobSkill.builder()
                                .job(savedJob)
                                .skill(skill)
                                .build())
                        .toList();
                jobSkillRepository.saveAll(jobSkills);
            }

            return savedJob;
        } catch (Exception e) {
            throw new Exception("Error creating job: " + e.getMessage());
        }
    }


    @Override
    public Page<JobDTO> getAllJobs(PageRequest pageRequest, String username) throws Exception {
        List<JobStatus> statuses = List.of(JobStatus.OPEN, JobStatus.HOT);
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest sortedPageRequest = PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize(), sort);
        Page<Job> jobs = jobRepository.findByStatusIn(statuses, sortedPageRequest);
        if (jobs.isEmpty()) {
            throw new Exception("No jobs found");
        }
        return jobs.map(job -> {
            try {
                return convertDTO(job, username);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public Page<JobDTO> getAllJobsAdmin(PageRequest pageRequest, String keyword, String status, String type,  String username) throws Exception {
        Specification<Job> spec = Specification.where(null);
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and(JobSpecifications.hasKeyword(keyword));
        }

        if (type != null && !type.isEmpty()) {
            spec = spec.and(JobSpecifications.hasJobType(type));
        }
        if (status != null && !status.isEmpty()) {
            spec = spec.and(JobSpecifications.hasStatus(status));
        }
        Page<Job> jobs = jobRepository.findAll(spec, pageRequest);

        if (jobs.isEmpty()) {
            throw new Exception("No jobs found");
        }

        return jobs.map(job -> {
            try {
                return convertDTO(job, username);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


    public JobDTO convertDTO(Job job, String username) throws Exception {

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
        if (job.getCompany() != null) {
            companyDTO = CompanyDTO.builder()
                    .name(job.getCompany().getName() == null ? "" : job.getCompany().getName())
                    .logo(job.getCompany().getLogo() == null ? "" : job.getCompany().getLogo())
                    .address(job.getCompany().getAddress() == null ? "" : job.getCompany().getAddress())
                    .webUrl(job.getCompany().getWebUrl() == null ? "" : job.getCompany().getWebUrl())
                    .build();
        }
        String applicationStatus = null;
        boolean liked = false;
        if (username != null) {
            User user = userService.findByUserName(username);
            FavoriteJob favoriteJob = favoriteJobRepository.findByUserAndJob(user, job);
            if (favoriteJob != null) {
                liked = true;
            }
            JobApplication jobApplications = jobApplicationRepository.findByJobIdAndUserId(job.getId(), user.getId());
            if (jobApplications != null) {
                applicationStatus = jobApplications.getStatus().name();
            }
        }
        boolean isHighLight = false;
        if (job.getHighlightEndTime() != null) {
            isHighLight = job.getHighlightEndTime().isAfter(LocalDateTime.now());
        }

        return JobDTO.builder()
                .id(job.getId())
                .isClose(getIsClose(job))
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
                .updatedAt(job.getUpdatedAt())
                .createdAt(job.getCreatedAt())
                .slots(job.getSlots())
                .views(job.getViews())
                .status(job.getStatus().name())
                .addresses(addressDTOs)
                .skills(skillDTOs)
                .company(companyDTO)
                .isFavorite(liked)
                .applyStatus(applicationStatus)
                .applyNumber(job.getApplyNumber())
                .isHighlight(isHighLight)
                .category(CategoryDTO.builder()
                        .id(job.getCategory().getId())
                        .name(job.getCategory().getName())
                        .build())
                .build();
    }

    @Override
    public Page<JobDTO> filterJobs(PageRequest pageRequest, JobFilterDTO jobFilterDTO, String username) {
        // Tạm thời sort theo createdAt DESC
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));
        PageRequest sortedPageRequest = PageRequest.of(
                pageRequest.getPageNumber(),
                pageRequest.getPageSize(),
                sort
        );

        List<Long> jobIdsBySkill = null;
        if (jobFilterDTO.getSkills() != null && !jobFilterDTO.getSkills().isEmpty()) {
            String skillNames = jobFilterDTO.getSkills().toLowerCase();
            List<JobStatus> statuses = List.of(JobStatus.OPEN, JobStatus.HOT);
            jobIdsBySkill = jobRepository.findJobIdsBySkillKeywordNative(skillNames, statuses);
            if (jobIdsBySkill.isEmpty()) {
                throw new EntityNotFoundException("No jobs found with the given skills.");
            }
        }

        Page<Job> jobPage = jobRepository.findAll(JobSpecifications.withCriteria(jobFilterDTO, jobIdsBySkill), sortedPageRequest);
        if (jobPage.isEmpty()) {
            throw new EntityNotFoundException("No jobs found with the given filter.");
        }

        List<JobDTO> sortedJobDTOs = jobPage.getContent().stream()
                .sorted(Comparator
                        .comparingInt((Job job) -> {
                            return switch (job.getStatus()) {
                                case HOT -> 0;
                                case OPEN -> 1;
                                default -> 2;
                            };
                        })
                        .thenComparing(Job::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                )
                .map(job -> {
                    try {
                        return convertDTO(job, username);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to convert Job to JobDTO", e);
                    }
                })
                .toList();

        return new PageImpl<>(sortedJobDTOs, pageRequest, jobPage.getTotalElements());
    }

    @Override
    public Job findById(Long jobId) throws Exception {
        return jobRepository.findById(jobId).orElseThrow(() -> new Exception("Job not found"));
    }

    @Override
    public JobApplication applyForJob(Long jobId, ApplyJobRequestDTO applyJobRequestDTO) throws Exception {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new Exception("Job not found with id: " + jobId));
        User user = userService.findById(applyJobRequestDTO.getUserId());

        CV cv = cvRepository.findById(applyJobRequestDTO.getCvId())
                .orElseThrow(() -> new Exception("CV not found with id: " + applyJobRequestDTO.getCvId()));

        JobApplication jobApplication = JobApplication
                .builder()
                .job(job)
                .cv(cv)
                .user(user)
                .letter(applyJobRequestDTO.getLetter())
                .status(JobApplicationStatus.IN_PROGRESS)
                .build();
        job.setApplyNumber(job.getApplyNumber() + 1);
        jobRepository.save(job);
        return jobApplicationRepository.save(jobApplication);
    }

    @Override
    public Page<JobDTO> getJobsByCompany(PageRequest pageRequest, String title, String status, String type, String username) throws Exception {
        try {
            Company company = companyService.findByUser(username);
            if (company == null) {
                throw new Exception("Company not found");
            }
            Specification<Job> spec = Specification.where(JobSpecifications.hasCompanyId(company.getId()));
            if (title != null && !title.isEmpty()) {
                spec = spec.and(JobSpecifications.hasKeyword(title));
            }
            if (status != null && !status.isEmpty()) {
                spec = spec.and(JobSpecifications.hasStatus(status));
            }
            if (type != null && !type.isEmpty()) {
                spec = spec.and(JobSpecifications.hasJobType(type));
            }

            Page<Job> jobs = jobRepository.findAll(spec, pageRequest);
            if (jobs.isEmpty()) {
                throw new Exception("No jobs found for that company");
            }
            return jobs.map(job -> {
                try {
                    return convertDTO(job, username);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            throw new Exception("Error retrieving jobs for company: " + e.getMessage());
        }
    }

    @Override
    public void likeJob(Long jobId, String username) throws Exception {
        try {
            Job job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new Exception("Job not found with id: " + jobId));
            User user = userService.findByUserName(username);
            if (user == null) {
                throw new Exception("User not found");
            }
            if (!favoriteJobService.addFavorite(job, user)) {
                throw new Exception("User already liked this job");
            }

            job.setLikeNumber(job.getLikeNumber() + 1);
            jobRepository.save(job);

        } catch (Exception e) {
            throw new Exception("Error liking job: " + e.getMessage());
        }
    }

    @Override
    public JobListResponse getFavoriteJobs(User user) throws Exception {
        try {
            Collection<FavoriteJob> favoriteJob = favoriteJobService.getFavoriteJobs(user);
            List<JobDTO> jobDTOs = favoriteJob.stream()
                    .map(favJob -> {
                                try {
                                    return convertDTO(favJob.getJob(), user.getUsername());
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    ).toList();
            if (jobDTOs.isEmpty()) {
                throw new Exception("No favorite jobs found");
            }
            return JobListResponse.builder()
                    .jobs(jobDTOs)
                    .build();
        } catch (Exception e) {
            throw new Exception("Error retrieving favorite jobs: " + e.getMessage());
        }
    }

    @Override
    public List<JobDTO> getJobsByCompanyId(Long companyId, String username) throws Exception {
        try {
            List<Job> jobs = jobRepository.findByCompanyId(companyId);
            if (jobs.isEmpty()) {
                throw new Exception("No jobs found for company with id: " + companyId);
            }
            return jobs.stream().map(job -> {
                try {
                    return convertDTO(job, username);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).toList();
        } catch (Exception e) {
            throw new Exception("Error retrieving jobs for company: " + e.getMessage());
        }
    }

    @Override
    public void approveJob(Long jobId) throws Exception {
        try {
            Job job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new Exception("Job not found with id: " + jobId));
            job.setStatus(EnumUtil.getEnumFromString(JobStatus.class, "OPEN"));
            job.setCreatedAt(LocalDateTime.now());
            notificationService.createAndSendNotification("Congratulations! Your job listing for " + job.getTitle() + " has been approved and is now visible to applicants.", job.getCompany().getCreatedBy().getUsername());
            jobRepository.save(job);
        } catch (Exception e) {
            throw new Exception("Error approving job: " + e.getMessage());
        }
    }

    @Override
    public void rejectJob(Long jobId) throws Exception {
        try {
            Job job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new Exception("Job not found with id: " + jobId));
            job.setStatus(EnumUtil.getEnumFromString(JobStatus.class, "REJECTED"));
            notificationService.createAndSendNotification("Unfortunately, your job listing for " + job.getTitle() + " has been rejected. Please review and update the job listing if necessary.", job.getCompany().getCreatedBy().getUsername());
            jobRepository.save(job);
        } catch (Exception e) {
            throw new Exception("Error rejecting job: " + e.getMessage());
        }
    }

    @Override
    public void expiredJob(Long jobId) throws Exception {
        try {
            Job job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new Exception("Job not found with id: " + jobId));
            job.setStatus(EnumUtil.getEnumFromString(JobStatus.class, "CLOSED"));
            notificationService.createAndSendNotification("Your job listing for " + job.getTitle() + " has expired. You may choose to renew or update the job listing to make it visible to applicants again.", job.getCompany().getCreatedBy().getUsername());
            jobRepository.save(job);
        } catch (Exception e) {
            throw new Exception("Error expiring job: " + e.getMessage());
        }
    }

    @Override
    public void vipJob(Long jobId) throws Exception {
        try {
            Job job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new Exception("Job not found with id: " + jobId));
            job.setStatus(EnumUtil.getEnumFromString(JobStatus.class, "HOT"));
            jobRepository.save(job);
        } catch (Exception e) {
            throw new Exception("Error making job VIP: " + e.getMessage());
        }
    }

    @Override
    public Page<JobDTO> searchJobs(PageRequest pageRequest, String keyword, String location, String jobType, String username) {
        Specification<Job> spec = Specification.where(null);

        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and(JobSpecifications.hasKeyword(keyword));
        }

        if (location != null && !location.isEmpty()) {
            spec = spec.and(JobSpecifications.hasLocation(location));
        }

        if (jobType != null && !jobType.isEmpty()) {
            spec = spec.and(JobSpecifications.hasJobType(jobType));
        }
        List<JobStatus> statuses = List.of(JobStatus.OPEN, JobStatus.HOT);
        spec = spec.and(JobSpecifications.hasStatuses(statuses));

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest sortedPageRequest = PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize(), sort);

        // Lấy các job với các điều kiện lọc và sắp xếp
        Page<Job> jobs = jobRepository.findAll(spec, sortedPageRequest);
        return jobs.map(job -> {
            try {
                return convertDTO(job, username);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public Page<JobDTO> getAppliedJobs(PageRequest pageRequest, String username) throws Exception {
        User user = userService.findByUserName(username);
        if (user == null) {
            throw new Exception("User not found");
        }
        Page<JobApplication> jobApplications = jobApplicationService.findByUserId(user.getId(), pageRequest);
        if (jobApplications.isEmpty()) {
            throw new Exception("You have not applied for any jobs");
        }
        return jobApplications.map(jobApplication -> JobDTO.builder()
                .id(jobApplication.getJob().getId())
                .isClose(getIsClose(jobApplication.getJob()))
                .title(jobApplication.getJob().getTitle())
                .salaryEnd(jobApplication.getJob().getSalaryEnd())
                .salaryStart(jobApplication.getJob().getSalaryStart())
                .type(jobApplication.getJob().getType().name())
                .currency(jobApplication.getJob().getCurrency().name())
                .deadline(jobApplication.getJob().getDeadline())
                .createdAt(jobApplication.getJob().getCreatedAt())
                .experience(jobApplication.getJob().getExperience())
                .position(jobApplication.getJob().getPosition())
                .level(jobApplication.getJob().getLevel())
                .company(CompanyDTO.builder()
                        .id(jobApplication.getJob().getCompany().getId())
                        .name(jobApplication.getJob().getCompany().getName())
                        .logo(jobApplication.getJob().getCompany().getLogo())
                        .build())
                .applyStatus(jobApplication.getStatus().name())
                .cvUrl(jobApplication.getCv().getCvUrl())
                .dateApplied(jobApplication.getUpdatedAt())
                .build());
    }

    @Override
    public int countJobsByCompanyId(String username) throws Exception {
        Company company = companyService.findByUser(username);
        return jobRepository.countJobsByCompany(company.getId());
    }

    @Override
    public int countPendingJobsByCompanyId(String username) {
        Company company = companyService.findByUser(username);
        return jobRepository.countPendingJobsByCompanyId(company.getId());
    }

    @Override
    public List<JobDTO> getLatestJobs(String username) throws Exception {
        try {
            // Tìm công ty theo tên người dùng
            Company company = companyService.findByUser(username);
            if (company == null) {
                throw new Exception("Company not found");
            }

            // Lấy 5 công việc mới nhất của công ty
            List<Job> jobs = jobRepository.findTop5ByCompanyIdOrderByCreatedAtDesc(company.getId());

            // Chuyển đổi danh sách công việc sang JobDTO
            return jobs.stream()
                    .map(job -> {
                        JobDTO jobDTO = new JobDTO();
                        jobDTO.setId(job.getId());
                        jobDTO.setTitle(job.getTitle());
                        jobDTO.setDeadline(job.getDeadline());
                        jobDTO.setSlots(job.getSlots());
                        jobDTO.setStatus(job.getStatus().name());
                        return jobDTO;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Xử lý lỗi nếu xảy ra và ném ra lỗi phù hợp
            throw new Exception("Error fetching latest jobs: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void editJob(Long jobId, JobDTO jobDTO) throws Exception {
        try {
            Job job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new Exception("Job not found with id: " + jobId));

            job.setTitle(jobDTO.getTitle());
            job.setDescription(jobDTO.getDescription());
            job.setSalaryStart(jobDTO.getSalaryStart());
            job.setSalaryEnd(jobDTO.getSalaryEnd());
            job.setType(EnumUtil.getEnumFromString(JobType.class, jobDTO.getType()));
            job.setCurrency(EnumUtil.getEnumFromString(Currency.class, jobDTO.getCurrency()));
            job.setExperience(jobDTO.getExperience());
            job.setPosition(jobDTO.getPosition());
            job.setLevel(jobDTO.getLevel());
            job.setRequirement(jobDTO.getRequirement());
            job.setBenefit(jobDTO.getBenefit());
            job.setDeadline(jobDTO.getDeadline());
            job.setSlots(jobDTO.getSlots());
            job.setStatus(JobStatus.valueOf("PENDING"));
            job.setCategory(categoryService.findById(jobDTO.getCategory().getId()));

            if (jobDTO.getAddresses() != null) {
                // Cleanup existing addresses
                jobAddressRepository.deleteAllByJobId(job.getId());

                // Map and save new addresses
                List<Address> addresses = jobDTO.getAddresses().stream()
                        .map(addressService::createAddress).toList();

                List<JobAddress> jobAddresses = addresses.stream()
                        .map(address -> JobAddress.builder()
                                .job(job)
                                .address(address)
                                .build())
                        .toList();

                jobAddressRepository.saveAll(jobAddresses);
            }

            if (jobDTO.getSkills() != null) {
                jobSkillRepository.deleteAllByJobId(job.getId());

                List<Skill> skills = jobDTO.getSkills().stream()
                        .map(skillService::createSkill).toList();

                List<JobSkill> jobSkills = skills.stream()
                        .map(skill -> JobSkill.builder()
                                .job(job)
                                .skill(skill)
                                .build())
                        .toList();

                jobSkillRepository.saveAll(jobSkills);
            }

            jobRepository.save(job);
        } catch (Exception e) {
            throw new Exception("Error editing job: " + e.getMessage());
        }
    }

    @Override
    public int countJobs() throws Exception {
        return jobRepository.countJobs();
    }

    @Override
    public int countJobsMonthly(int month, int year) throws Exception {
        return jobRepository.countJobsMonthly(month, year);
    }

    @Override
    public List<MonthlyCountResponse> countJobsByMonth(int year) throws Exception {
        return jobRepository.countJobsByMonth(year);
    }

    @Override
    public void closeJob(Long jobId) throws Exception {
        try {
            Job job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new Exception("Job not found with id: " + jobId));
            job.setStatus(EnumUtil.getEnumFromString(JobStatus.class, "CLOSED"));
            jobRepository.save(job);
        } catch (Exception e) {
            throw new Exception("Error closing job: " + e.getMessage());
        }
    }

    @Override
    public List<JobDTO> get5LatestJobs() throws Exception {
        try {
            List<Job> jobs = jobRepository.findTop5ByOrderByCreatedAtDesc();
            return jobs.stream()
                    .map(job -> {
                        try {
                            return convertDTO(job, null);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
        } catch (Exception e) {
            throw new Exception("Error fetching latest jobs: " + e.getMessage());
        }
    }

    @Override
    public int countJobsByStatusIn(List<JobStatus> statuses) throws Exception {
        return jobRepository.countJobsByStatusIn(statuses);
    }

    @Override
    public void increaseView(Long jobId) throws Exception {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new Exception("Job not found"));
        job.setViews(job.getViews() + 1);
        jobRepository.save(job);
    }

    @Override
    public List<JobDTO> getRelatedJobs(Long jobId) throws Exception {
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new Exception("Job not found"));

        List<Job> relatedJobs = jobRepository.getRelatedJobs(jobId, List.of(JobStatus.OPEN, JobStatus.HOT));

        if (relatedJobs.size() < 6) {
            Specification<Job> spec = Specification.where(JobSpecifications.hasStatuses(List.of(JobStatus.OPEN, JobStatus.HOT)));

            Pageable pageable = PageRequest.of(0, 7, Sort.by(Sort.Order.desc("views")));

            List<Job> additionalJobs = jobRepository.findAll(spec, pageable).getContent();

            for (Job additionalJob : additionalJobs) {
                if (relatedJobs.size() >= 6) {
                    break;
                }

                if (!relatedJobs.contains(additionalJob) && !additionalJob.getId().equals(jobId)) {
                    relatedJobs.add(additionalJob);
                }
            }
        }

        return relatedJobs.stream()
                .map(relatedJob -> {
                    try {
                        return convertDTO(relatedJob, null);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<JobDTO> getJobsByCompanyIdForCandidate(Long companyId) throws Exception {
        List<Job> jobs = jobRepository.findByCompanyId(companyId);
        return jobs.stream()
                .filter(job -> job.getStatus() == JobStatus.OPEN || job.getStatus() == JobStatus.HOT)
                .map(job -> {
                    try {
                        return convertDTO(job, null);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<JobDTO> getJobsByCategoryIds(PageRequest pageRequest, List<Long> categoryIds) {
        List<JobStatus> statuses = List.of(JobStatus.OPEN, JobStatus.HOT);
        Page<Job> jobs = jobRepository.getJobsByCategoryIds(categoryIds, statuses, pageRequest);
        return jobs.map(job -> {
            try {
                return convertDTO(job, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public Long countJobsByCategoryId(Long categoryId) {
        return jobRepository.countJobsByCategoryId(categoryId);
    }

    @Override
    public List<JobDTO> getNewJobsForUser(String email) throws Exception {
        User user = userService.findByUserName(email);

        List<Job> jobs = jobRepository.findSimilarJobs(
                user.getId(),
                LocalDateTime.now().minusDays(30) // Chỉ lấy job trong 7 ngày gần nhất
        );

        return jobs.stream()
                .filter(job -> job.getStatus() == JobStatus.OPEN || job.getStatus() == JobStatus.HOT)
                .map(job -> {
                    try {
                        return convertDTO(job, email);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    private boolean getIsClose(Job job) {
        if (job.getStatus() == JobStatus.CLOSED)
            return  true;
        return false;
    }
}
