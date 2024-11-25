package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.CompanyReviewDTO;
import com.hcmute.devhire.entities.CompanyReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ICompanyReviewService {
    CompanyReview addReview(Long companyId, String username, CompanyReviewDTO companyReviewDTO) throws Exception;
    Page<CompanyReviewDTO> getReviewsByCompanyId(PageRequest pageRequest, Long companyId) throws Exception;
}
