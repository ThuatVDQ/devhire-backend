package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.CompanyDTO;
import com.hcmute.devhire.DTOs.UserDTO;
import com.hcmute.devhire.entities.*;
import com.hcmute.devhire.repositories.CompanyRepository;
import com.hcmute.devhire.repositories.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService implements ICompanyService {
    private final CompanyRepository companyRepository;
    private final IUserService userService;
    private final JobRepository jobRepository;
    @Override
    public Company createCompany(CompanyDTO companyDTO, String username) throws Exception {
        UserDTO user = userService.findByUsername(username);
        if (user == null) {
            throw new Exception("User not found");
        }
        if (user.getRoleId() != 2) {
            throw new Exception("User is not create a company");
        }

        Company newCompany = Company.builder()
                .name(companyDTO.getName())
                .taxCode(companyDTO.getTaxCode())
                .logo(companyDTO.getLogo() == null ? "" : companyDTO.getLogo())
                .address(companyDTO.getAddress())
                .description(companyDTO.getDescription() == null ? "" : companyDTO.getDescription())
                .email(companyDTO.getEmail())
                .phone(companyDTO.getPhone())
                .webUrl(companyDTO.getWebUrl() == null ? "" : companyDTO.getWebUrl())
                .scale(companyDTO.getScale() == 0 ? 0 : companyDTO.getScale())
                .status("ACTIVE")
                .createdBy(userService.findById(user.getId()))
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
                .totalJob(company.getJobs().size())
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

    public Set<Skill> getAllSkillsForCompany(Long companyId) {
        // Lấy tất cả các job của công ty
        List<Job> jobs = jobRepository.findByCompanyId(companyId);

        // Lấy tất cả các kỹ năng từ danh sách job
        return jobs.stream()
                .flatMap(job -> job.getJobSkills().stream()) // Lấy danh sách JobSkill từ mỗi Job
                .map(JobSkill::getSkill) // Truy xuất Skill từ JobSkill
                .collect(Collectors.toSet()); // Sử dụng Set để loại bỏ kỹ năng trùng lặp
    }

    public Set<Address> getAllAddressesForCompany(Long companyId) {
        List<Job> jobs = jobRepository.findByCompanyId(companyId);

        return jobs.stream()
                .flatMap(job -> job.getJobAddresses().stream())
                .map(JobAddress::getAddress)
                .collect(Collectors.toSet());
    }
}
