package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.CompanyDTO;
import com.hcmute.devhire.DTOs.UserDTO;
import com.hcmute.devhire.components.CompanyScoreCalculator;
import com.hcmute.devhire.components.FileUtil;
import com.hcmute.devhire.components.JwtUtil;
import com.hcmute.devhire.entities.*;
import com.hcmute.devhire.repositories.CompanyImageRepository;
import com.hcmute.devhire.repositories.CompanyRepository;
import com.hcmute.devhire.repositories.CompanyReviewRepository;
import com.hcmute.devhire.repositories.JobRepository;
import com.hcmute.devhire.repositories.specification.CompanySpecifications;
import com.hcmute.devhire.utils.CompanyStatus;
import com.hcmute.devhire.utils.JobStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Comparator;
import java.util.Objects;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService implements ICompanyService {
    private final CompanyRepository companyRepository;
    private final IUserService userService;
    private final JobRepository jobRepository;
    private final CompanyImageRepository companyImageRepository;
    private final FileUtil fileUtil;
    private final ICompanyImageService companyImageService;
    private final INotificationService notificationService;
    private final CompanyScoreCalculator companyScoreCalculator;
    private final CompanyReviewRepository companyReviewRepository;
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
                .phoneVerified(false)
                .companyStatus(String.valueOf(CompanyStatus.PENDING))
                .createdBy(userService.findById(user.getId()))
                .build();
        return companyRepository.save(newCompany);
    }

    @Override
    public Page<CompanyDTO> getAllCompanies(PageRequest pageRequest) throws Exception {
        try {
            List<Company> companies = companyRepository.findAll(); // Lấy toàn bộ để tính điểm

            if (companies.isEmpty()) {
                throw new Exception("No company found");
            }

            // Tính điểm và map sang DTO
            List<CompanyDTO> scoredCompanies = companies.stream()
                    .map(company -> {
                        try {
                            CompanyDTO dto = convertDTO(company);
                            List<Job> jobs = jobRepository.findByCompanyId(company.getId());
                            List<CompanyReview> reviews = companyReviewRepository.findByCompanyId(company.getId());
                            double score = companyScoreCalculator.calculateScore(company, reviews, jobs);
                            dto.setScore(score); // Yêu cầu: CompanyDTO phải có trường score
                            return dto;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .sorted(Comparator.comparingDouble(CompanyDTO::getScore).reversed()) // Sắp xếp giảm dần
                    .toList();

            // Thực hiện phân trang thủ công
            int start = (int) pageRequest.getOffset();
            int end = Math.min(start + pageRequest.getPageSize(), scoredCompanies.size());

            List<CompanyDTO> pageContent = scoredCompanies.subList(start, end);
            return new PageImpl<>(pageContent, pageRequest, scoredCompanies.size());

        } catch (Exception e) {
            throw new Exception("Error when get all companies");
        }
    }

    public CompanyDTO convertDTO(Company company) throws Exception {

        List<String> images = companyImageService.getImageUrlsByCompanyId(company.getId());

        List<Job> jobs = company.getJobs();
        jobs = jobs.stream()
                .filter(job -> job.getStatus().equals(JobStatus.OPEN) || job.getStatus().equals(JobStatus.HOT))
                .toList();

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
                .totalJob(jobs.size())
                .totalReviews(company.getCompanyReviews().size())
                .images(images == null ? List.of() : images)
                .phoneVerified(company.getPhoneVerified())
                .companyStatus(company.getCompanyStatus())
                .businessLicense(company.getBusinessLicense() == null ? "" : company.getBusinessLicense())

                .build();
    }
    @Override
    public Company findByUser(String username) {
        return companyRepository.findByUser(username);
    }

    @Override
    public CompanyDTO getByUser(String username) throws Exception {
        Company company = companyRepository.findByUser(username);
        if (company == null) {
            return null;
        }

        return convertDTO(company);
    }

    @Override
    public Company updateCompany(CompanyDTO companyDTO, String username) throws Exception {
        UserDTO user = userService.findByUsername(username);
        if (user == null) {
            throw new Exception("User not found");
        }
        if (user.getRoleId() != 2) {
            throw new Exception("User is not create a company");
        }
        Company company = companyRepository.findByUser(username);
        if (companyDTO.getName() != null) {
            company.setName(companyDTO.getName());
        }
        if (companyDTO.getTaxCode() != null) {
            company.setTaxCode(companyDTO.getTaxCode());
        }
        if (companyDTO.getDescription() != null) {
            company.setDescription(companyDTO.getDescription());
        }
        if (companyDTO.getScale() != 0) {
            company.setScale(companyDTO.getScale());
        }
        if (companyDTO.getWebUrl() != null) {
            company.setWebUrl(companyDTO.getWebUrl());
        }
        if (companyDTO.getAddress() != null) {
            company.setAddress(companyDTO.getAddress());
        }

        if (companyDTO.getEmail() != null) {
            company.setEmail(companyDTO.getEmail());
        }

        if (companyDTO.getPhone() != null) {
            company.setPhone(companyDTO.getPhone());
        }

        return companyRepository.save(company);
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

    @Override
    public CompanyDTO getCompanyById(Long companyId) throws Exception {
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new Exception("Company not found"));
        return convertDTO(company);
    }

    @Override
    public Company getCompanyByID(Long companyId) throws Exception {
        return companyRepository.findById(companyId).orElseThrow(() -> new Exception("Company not found"));
    }

    @Override
    public void uploadLogo(MultipartFile file, String username) throws IOException {
        if (fileUtil.isImageFormatValid(file)) {
            String filename = fileUtil.storeFile(file);
            Company company = companyRepository.findByUser(username);
            company.setLogo(filename);
            companyRepository.save(company);
        } else {
            throw new IOException("Invalid image format");
        }
    }

    @Override
    public Page<CompanyDTO> searchCompanies(Pageable pageable, String keyword) throws Exception {
        Specification<Company> spec = Specification.where(null);
        if (!Objects.isNull(keyword) && !keyword.isEmpty()) {
            spec = spec.and(CompanySpecifications.hasKeyword(keyword));
        }
        try {
            Page<Company> companies = companyRepository.findAll(spec, pageable);
            if (companies.isEmpty()) {
                throw new Exception("No company found");
            }
            return companies.map(company -> {
                try {
                    return convertDTO(company);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            throw new Exception("Error when search companies");
        }
    }

    @Override
    public int countCompanies() throws Exception {
        return companyRepository.countCompanies();
    }

    @Override
    public int countCompaniesMonthly(int month, int year) throws Exception {
        return companyRepository.countCompaniesMonthly(month, year);
    }

    @Override
    public void updateCompanyImages(List<String> oldImages, MultipartFile[] images, String username) throws IOException {
        Company company = companyRepository.findByUser(username);
        if (oldImages.size() > 0) {
            List<CompanyImage> imagesToRemove = companyImageRepository.findAllByCompanyAndImageUrlNotIn(company, oldImages);
            for (CompanyImage image : imagesToRemove) {
                fileUtil.deleteFile(image.getImageUrl());
                companyImageRepository.delete(image);
            }
        } else {
            List<CompanyImage> imagesToRemove = companyImageRepository.findByCompanyId(company.getId());
            for (CompanyImage image : imagesToRemove) {
                fileUtil.deleteFile(image.getImageUrl());
                companyImageRepository.delete(image);
            }
        }

        if (images != null) {
            for (MultipartFile image : images) {
                if (fileUtil.isImageFormatValid(image)) {
                    String filename = fileUtil.storeFile(image);

                    CompanyImage companyImage = CompanyImage.builder()
                            .imageUrl(filename)
                            .company(company)
                            .build();

                    companyImageRepository.save(companyImage);
                } else {
                    throw new IOException("Invalid image format for file: " + image.getOriginalFilename());
                }
            }
        }
    }

    @Override
    public List<CompanyDTO> getRelatedCompanies(Long companyId) throws Exception {
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new Exception("Company not found"));
        Pageable pageable = PageRequest.of(0, 7);

        List<Company> relatedCompanies = companyRepository.getRelatedCompanies(companyId, pageable);
        if (relatedCompanies.size() < 6) {
            Pageable randomPageable = PageRequest.of(0, 12);
            List<Company> additionalCompanies = companyRepository.findRandomCompanies(randomPageable);

            for (Company additionalCompany : additionalCompanies) {
                if (relatedCompanies.size() >= 6) {
                    break;
                }
                if (!relatedCompanies.contains(additionalCompany) && !additionalCompany.getId().equals(companyId)) {
                    relatedCompanies.add(additionalCompany);
                }
            }
        }
        return relatedCompanies.stream()
                .map(relatedCompany -> {
                    try {
                        return convertDTO(relatedCompany);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public void updateLicense(String license, String username) {
        Company company = companyRepository.findByUser(username);
        company.setBusinessLicense(license);
        companyRepository.save(company);
    }

    @Override
    public void approveCompany(Long companyId) throws Exception {
        Company company = companyRepository.findById(companyId).orElse(null);
        String username = JwtUtil.getAuthenticatedUsername();
        User user = userService.findByUserName(username);

        if (company != null && user.getRole().getId() == 1) {
            company.setCompanyStatus(String.valueOf(CompanyStatus.ACTIVE));
            companyRepository.save(company);
            notificationService.createAndSendNotification("Your company has been approved", company.getCreatedBy().getUsername());
        } else {
            throw new RuntimeException("Company not found or you are not authorized to approve company");
        }
    }

    @Override
    public void rejectCompany(Long companyId, String reason) throws Exception {
        Company company = companyRepository.findById(companyId).orElse(null);
        String username = JwtUtil.getAuthenticatedUsername();
        User user = userService.findByUserName(username);
        if (company != null && user.getRole().getId() == 1) {
            company.setCompanyStatus(String.valueOf(CompanyStatus.REJECTED));
            companyRepository.save(company);
            notificationService.createAndSendNotification("Your company has been rejected with reason: " + reason, company.getCreatedBy().getUsername());
        } else {
            throw new RuntimeException("Company not found");
        }
    }
}
