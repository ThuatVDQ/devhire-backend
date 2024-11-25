package com.hcmute.devhire.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hcmute.devhire.DTOs.CompanyReviewDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyReviewListResponse {
    @JsonProperty("company_reviews")
    List<CompanyReviewDTO> companyReviews;
    int totalPages;
    int currentPage;
    int pageSize;
    Long totalElements;
}
