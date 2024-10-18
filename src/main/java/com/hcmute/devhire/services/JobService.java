package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.ApplyJobRequestDTO;
import com.hcmute.devhire.DTOs.JobApplicationDTO;
import com.hcmute.devhire.DTOs.JobDTO;
import com.hcmute.devhire.entities.*;
import com.hcmute.devhire.repositories.JobApplicationRepository;
import com.hcmute.devhire.repositories.JobRepository;
import com.hcmute.devhire.utils.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService implements IJobService{
    private final JobRepository jobRepository;
    private final CategoryService categoryService;
    private final JobAddressService jobAddressService;
    private final JobSkillService jobSkillService;
    private final IUserService userService;
    private final ICVService cvService;
    private final JobApplicationRepository jobApplicationRepository;
    private final IAddressService addressService;
    private final ISkillService skillService;
    @Override
    public Job createJob(JobDTO jobDTO) {
        JobType jobType = EnumUtil.getEnumFromString(JobType.class, jobDTO.getType());
        Currency currency = EnumUtil.getEnumFromString(Currency.class, jobDTO.getCurrency());
        JobStatus status = EnumUtil.getEnumFromString(JobStatus.class, "CLOSED");

        Category category = categoryService.findById(jobDTO.getCategory().getId());

        List<Address> addresses = jobDTO.getJobAddresses().stream()
                .map(addressService::createAddress).toList();

        List<Skill> skills = jobDTO.getJobSkills().stream()
                .map(skillService::createSkill).toList();

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
                .build();

        return jobRepository.save(newJob);
    }

    @Override
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    @Override
    public Job findById(Long jobId) {
        return jobRepository.findById(jobId).orElse(null);
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
}
