package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.JobAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobAddressRepository extends JpaRepository<JobAddress, Long> {
}
