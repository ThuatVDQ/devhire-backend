package com.hcmute.devhire.responses;

import com.hcmute.devhire.entities.Company;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyListResponse {
    List<Company> companies;
    int totalPages;
}
