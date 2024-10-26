package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.*;
import com.hcmute.devhire.entities.*;
import com.hcmute.devhire.repositories.JobAddressRepository;
import com.hcmute.devhire.repositories.JobApplicationRepository;
import com.hcmute.devhire.repositories.JobRepository;
import com.hcmute.devhire.repositories.JobSkillRepository;
import com.hcmute.devhire.utils.*;
import lombok.RequiredArgsConstructor;
import org.springframework.expression.ExpressionException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService implements IJobService{
    private final JobRepository jobRepository;
    private final CategoryService categoryService;
    private final JobAddressRepository jobAddressRepository;
    private final JobSkillRepository jobSkillRepository;
    private final IUserService userService;
    private final ICVService cvService;
    private final JobApplicationRepository jobApplicationRepository;
    private final IAddressService addressService;
    private final ISkillService skillService;
    private final ICompanyService companyService;
    @Override
    public Job createJob(JobDTO jobDTO, String username) throws Exception {
        try {
            JobType jobType = EnumUtil.getEnumFromString(JobType.class, jobDTO.getType());
            Currency currency = EnumUtil.getEnumFromString(Currency.class, jobDTO.getCurrency());
            JobStatus status = EnumUtil.getEnumFromString(JobStatus.class, "CLOSED");

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
    public List<JobDTO> getAllJobs() {
        List<Job> jobs = jobRepository.findAll();
        return jobs.stream().map(job -> JobDTO.builder()
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
                .build()
        ).toList();
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

        CV cv = cvService.findById(applyJobRequestDTO.getCvId())
                .orElseThrow(() -> new Exception("CV not found with id: " + applyJobRequestDTO.getCvId()));

        JobApplication jobApplication = JobApplication
                .builder()
                .job(job)
                .cv(cv)
                .user(user)
                .status(JobApplicationStatus.IN_PROGRESS)
                .build();
        return jobApplicationRepository.save(jobApplication);
    }

    @Override
    public List<JobDTO> getJobsByCompany(String username) throws Exception {
        try {
            // Tìm công ty dựa trên username
            Company company = companyService.findByUser(username);
            if (company == null) {
                throw new Exception("Company not found");
            }

            // Tìm danh sách công việc dựa trên companyId
            List<Job> jobs = jobRepository.findByCompanyId(company.getId());

            // Map từ Job sang JobDTO
            return jobs.stream().map(job -> JobDTO.builder()
                    .id(job.getId())
                    .title(job.getTitle())
                    .salaryStart(job.getSalaryStart())
                    .salaryEnd(job.getSalaryEnd())
                    .type(job.getType().name())
                    .currency(job.getCurrency().name())
                    .deadline(job.getDeadline())
                    .slots(job.getSlots())
                    .status(job.getStatus().name())
                    .applyNumber(job.getApplyNumber())
                    .likeNumber(job.getLikeNumber())
                    .category(job.getCategory() != null ?
                            CategoryDTO.builder()
                                    .name(job.getCategory().getName())
                                    .build() : null)
                    .build()
            ).toList();
        } catch (Exception e) {
            throw new Exception("Error retrieving jobs for company: " + e.getMessage());
        }
    }
}
