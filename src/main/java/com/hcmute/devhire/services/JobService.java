package com.hcmute.devhire.services;

import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.repositories.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class JobService implements IJobService{
    private final JobRepository jobRepository;
    @Override
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }
}
