package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.CompanyImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyImageRepository extends JpaRepository<CompanyImage, Long> {
    List<CompanyImage> findByCompanyId(Long companyId);
}
