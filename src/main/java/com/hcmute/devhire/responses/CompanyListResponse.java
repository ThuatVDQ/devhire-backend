package com.hcmute.devhire.responses;

import com.hcmute.devhire.DTOs.CompanyDTO;
import com.hcmute.devhire.entities.Company;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyListResponse {
    List<CompanyDTO> companies;
    int totalPages;
    int currentPage;
    int pageSize;
    Long totalElements;
}
