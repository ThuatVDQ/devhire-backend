package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.CV;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CVRepository extends JpaRepository<CV, Long> {
    CV findByUserId(Long userId);
}
