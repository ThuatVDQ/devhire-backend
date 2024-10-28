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
    public Page<CompanyDTO> getAllCompanies(PageRequest pageRequest) {
        Page<Company> companies= companyRepository.findAll(pageRequest);
        return companies.map(this::convertDTO);
    }

    public CompanyDTO convertDTO(Company company) {
        return CompanyDTO.builder()
                .id(company.getId())
                .name(company.getName())
                .taxCode(company.getTaxCode())
                .logo(company.getLogo())
                .address(company.getAddress())
                .description(company.getDescription())
                .email(company.getEmail())
                .phone(company.getPhone())
                .webUrl(company.getWebUrl())
                .scale(company.getScale())
                .status(company.getStatus())
                .build();
    }
    @Override
    public Company findByUser(String username) {
        return companyRepository.findByUser(username);
    }

    @Override
    public CompanyDTO getByUser(String username) {
        Company company = companyRepository.findByUser(username);
        if (company == null) {
            return null;
        }

        return convertDTO(company);
    }
}
