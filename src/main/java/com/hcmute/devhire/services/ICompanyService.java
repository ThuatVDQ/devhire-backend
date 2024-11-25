package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.CompanyDTO;
import com.hcmute.devhire.entities.Address;
import com.hcmute.devhire.entities.Company;
import com.hcmute.devhire.entities.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface ICompanyService {
    Company createCompany(CompanyDTO companyDTO, String username) throws Exception;
    Page<CompanyDTO> getAllCompanies(PageRequest pageRequest) throws Exception;
    Company findByUser(String username);
    CompanyDTO getByUser(String username) throws Exception;
    Company updateCompany(CompanyDTO companyDTO, String username) throws Exception;
    Set<Skill> getAllSkillsForCompany(Long companyId);
    Set<Address> getAllAddressesForCompany(Long companyId);
    CompanyDTO getCompanyById(Long companyId) throws Exception;
    Company getCompanyByID(Long companyId) throws Exception;
    void uploadLogo(MultipartFile file, String username) throws IOException;
    Page<CompanyDTO> searchCompanies(Pageable pageable, String keyword) throws Exception;
    int countCompanies() throws Exception;
    int countCompaniesMonthly(int month, int year) throws Exception;
    void updateCompanyImages(List<String> oldImages, MultipartFile[] images, String username) throws IOException;
}
