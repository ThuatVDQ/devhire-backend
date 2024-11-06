package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    Page<Job> findAll(Pageable pageable);
    Optional<Job> findById(Long id);
    List<Job> findByCompanyId(Long companyId);
}
