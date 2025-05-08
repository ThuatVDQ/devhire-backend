package com.hcmute.devhire.repositories;

import com.hcmute.devhire.entities.CompanyReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyReviewRepository extends JpaRepository<CompanyReview, Long> {
    Page<CompanyReview> findByCompanyId(PageRequest pageRequest, Long companyId);
    List<CompanyReview> findByCompanyId(Long companyId);
}
