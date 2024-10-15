package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.JobDTO;
import com.hcmute.devhire.entities.Category;
import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.entities.Address;
import com.hcmute.devhire.entities.Skill;
import com.hcmute.devhire.repositories.JobRepository;
import com.hcmute.devhire.utils.Currency;
import com.hcmute.devhire.utils.EnumUtil;
import com.hcmute.devhire.utils.JobStatus;
import com.hcmute.devhire.utils.JobType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class JobService implements IJobService{
    private final JobRepository jobRepository;
    private final CategoryService categoryService;
    private final JobAddressService jobAddressService;
    private final JobSkillService jobSkillService;

    @Override
    public Job createJob(JobDTO jobDTO) {
        JobType jobType = EnumUtil.getEnumFromString(JobType.class, jobDTO.getType());
        Currency currency = EnumUtil.getEnumFromString(Currency.class, jobDTO.getCurrency());
        JobStatus status = EnumUtil.getEnumFromString(JobStatus.class, "CLOSED");

        Category category = categoryService.findById(jobDTO.getCategory().getId());

        List<Address> addresses = jobAddressService.createJobAddresses(jobDTO.getJobAddresses());

        List<Skill> skills = jobSkillService.createJobSkills(jobDTO.getJobSkills());

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
                .addresses(addresses)
                .skills(skills)
                .build();

        addresses.forEach(address -> address.setJob(newJob));
        skills.forEach(skill -> skill.setJob(newJob));
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
}
