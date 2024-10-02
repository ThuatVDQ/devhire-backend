package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {
}
