package com.hcmute.devhire.responses;

import com.hcmute.devhire.DTOs.CompanyDTO;
import com.hcmute.devhire.DTOs.JobApplicationDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationListResponse {
    List<JobApplicationDTO> applications;
    int totalPages;
    int currentPage;
    int pageSize;
    Long totalElements;
}
