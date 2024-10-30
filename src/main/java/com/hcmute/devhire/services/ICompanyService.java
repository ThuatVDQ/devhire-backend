package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.CompanyDTO;
import com.hcmute.devhire.entities.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public interface ICompanyService {
    Company createCompany(CompanyDTO companyDTO, String username) throws Exception;
    Page<CompanyDTO> getAllCompanies(PageRequest pageRequest);
    Company findByUser(String username);
    CompanyDTO getByUser(String username);
    Company updateCompany(CompanyDTO companyDTO, String username) throws Exception;
}
