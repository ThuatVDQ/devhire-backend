package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {
    Page<Job> findAll(Pageable pageable);
    Optional<Job> findById(Long id);
}
