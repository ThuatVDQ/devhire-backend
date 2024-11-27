package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.Company;
import com.hcmute.devhire.entities.CompanyImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyImageRepository extends JpaRepository<CompanyImage, Long> {
    List<CompanyImage> findByCompanyId(Long companyId);
    List<CompanyImage> findAllByCompanyAndImageUrlNotIn(Company company, List<String> imageUrls);
}
