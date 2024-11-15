package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.*;
import com.hcmute.devhire.entities.*;
import com.hcmute.devhire.repositories.*;
import com.hcmute.devhire.repositories.specification.JobSpecifications;
import com.hcmute.devhire.responses.JobListResponse;
import com.hcmute.devhire.utils.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.expression.ExpressionException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService implements IJobService{
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
            List<Address> addresses = jobDTO.getAddresses().stream()
                    .map(addressService::createAddress).toList();

            addresses.forEach(address -> {
                JobAddress jobAddress = JobAddress.builder()
                        .job(savedJob)
                        .address(address)
                        .build();
                jobAddressRepository.save(jobAddress);
            });
            List<Skill> skills = jobDTO.getSkills().stream()
                    .map(skillService::createSkill).toList();
            skills.forEach(skill -> {
                JobSkill jobSkill = JobSkill.builder()
                        .job(savedJob)
                        .skill(skill)
                        .build();
                jobSkillRepository.save(jobSkill);
            });
            return savedJob;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Page<JobDTO> getAllJobs(PageRequest pageRequest, String username) throws Exception {
        List<JobStatus> statuses = List.of(JobStatus.OPEN, JobStatus.HOT);
        Page<Job> jobs= jobRepository.findByStatusIn(statuses, pageRequest);

        return jobs.map(job -> {
            try {
                return convertDTO(job, username);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public Page<JobDTO> getAllJobsAdmin(PageRequest pageRequest, String username) throws Exception {
        Page<Job> jobs= jobRepository.findAll(pageRequest);

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
        return JobDTO.builder()
                .id(job.getId())
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
                .isFavorite(liked)
                .applyStatus(applicationStatus)
                .applyNumber(job.getApplyNumber())
                .category(CategoryDTO.builder()
                        .id(job.getCategory().getId())
                        .name(job.getCategory().getName())
                        .build())
                .build();
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
                .status(JobApplicationStatus.IN_PROGRESS)
                .build();
        job.setApplyNumber(job.getApplyNumber() + 1);
        jobRepository.save(job);
        return jobApplicationRepository.save(jobApplication);
    }

    @Override
    public Page<JobDTO> getJobsByCompany(PageRequest pageRequest, String username) throws Exception {
        try {
            Company company = companyService.findByUser(username);
            if (company == null) {
                throw new Exception("Company not found");
            }

            Page<Job> jobs = jobRepository.findByCompanyId(company.getId(), pageRequest);
            return jobs.map(job -> JobDTO.builder()
                    .id(job.getId())
                    .title(job.getTitle())
                    .salaryStart(job.getSalaryStart())
                    .salaryEnd(job.getSalaryEnd())
                    .type(job.getType().name())
                    .status(job.getStatus().name())
                    .category(CategoryDTO.builder().name(job.getCategory().getName()).build())
                    .applyNumber(job.getApplyNumber())
                    .build());
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

        Page<Job> jobs = jobRepository.findAll(spec, pageRequest);
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
        return jobApplications.map(jobApplication -> JobDTO.builder()
                .id(jobApplication.getJob().getId())
                .title(jobApplication.getJob().getTitle())
                .salaryEnd(jobApplication.getJob().getSalaryEnd())
                .salaryStart(jobApplication.getJob().getSalaryStart())
                .type(jobApplication.getJob().getType().name())
                .currency(jobApplication.getJob().getCurrency().name())
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
}
