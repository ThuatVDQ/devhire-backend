package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.CompanyDTO;
import com.hcmute.devhire.entities.Company;
import com.hcmute.devhire.repositories.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyService implements ICompanyService {
    private final CompanyRepository companyRepository;

    @Override
    public Company createCompany(CompanyDTO companyDTO) {
        Company newCompany = Company.builder()
                .name(companyDTO.getName())
                .taxCode(companyDTO.getTaxCode())
                .logo(companyDTO.getLogo())
                .address(companyDTO.getAddress())
                .description(companyDTO.getDescription())
                .email(companyDTO.getEmail())
                .phone(companyDTO.getPhone())
                .webUrl(companyDTO.getWebUrl())
                .scale(companyDTO.getScale())
                .status(companyDTO.getStatus())
                .build();
        return companyRepository.save(newCompany);
    }

    @Override
    public Page<Company> getAllCompanies(PageRequest pageRequest) {
        Page<Company> companies;
        companies = companyRepository.findAll(pageRequest);
        return companies;
    }

    @Override
    public Company findByUser(String username) {
        return companyRepository.findByUser(username);

    }
}
