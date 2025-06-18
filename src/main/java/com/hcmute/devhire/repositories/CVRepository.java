package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.CV;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CVRepository extends JpaRepository<CV, Long> {
    List<CV> findByUserId(Long userId);

    CV findTopByUserIdOrderByUpdatedAtDesc(Long userId);
}
