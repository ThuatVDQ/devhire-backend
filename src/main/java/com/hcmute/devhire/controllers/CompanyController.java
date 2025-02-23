package com.hcmute.devhire.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcmute.devhire.DTOs.CompanyDTO;
import com.hcmute.devhire.components.FileUtil;
import com.hcmute.devhire.components.JwtUtil;
import com.hcmute.devhire.entities.Address;
import com.hcmute.devhire.entities.Company;
import com.hcmute.devhire.entities.Skill;
import com.hcmute.devhire.responses.CompanyListResponse;
import com.hcmute.devhire.services.ICompanyService;
import com.hcmute.devhire.services.INotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/companies")
public class CompanyController {
    private final ICompanyService companyService;
    private final INotificationService notificationService;
    private final FileUtil fileUtil;

    @GetMapping("")
    public ResponseEntity<?> getAllCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        if (page < 0 || limit <= 0) {
            return ResponseEntity.badRequest().body("Page must be >= 0 and limit must be > 0.");
        }
        try {
            PageRequest pageRequest = PageRequest.of(
                    page, limit,
                    Sort.by("id").descending()
            );
            Page<CompanyDTO> companyPage = companyService.getAllCompanies(pageRequest);
            CompanyListResponse response = CompanyListResponse.builder()
                    .companies(companyPage.getContent())
                    .currentPage(page)
                    .pageSize(limit)
                    .totalPages(companyPage.getTotalPages())
                    .totalElements(companyPage.getTotalElements())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCompanyById(@PathVariable Long id) {
        try {
            CompanyDTO companyDTO = companyService.getCompanyById(id);
            if (companyDTO == null) {
                return ResponseEntity.badRequest().body("Company not found");
            }
            return ResponseEntity.ok(companyDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("")
    public ResponseEntity<?> createCompany(@Valid @RequestBody CompanyDTO companyDTO, BindingResult result) {

        if(result.hasErrors()) {
            List<String> errorMessage = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
            return ResponseEntity.badRequest().body(errorMessage);
        }
        try {
            String username = JwtUtil.getAuthenticatedUsername();
            if (username == null) {
                return ResponseEntity.badRequest().body("User not found");
            }
            Company newCompany = companyService.createCompany(companyDTO, username);
            notificationService.sendNotificationToAdmin("New company: " + newCompany.getName() + " has been created");
            return ResponseEntity.ok(newCompany);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/profile")
    public  ResponseEntity<?> getByCreated(){
        try {
            String username = JwtUtil.getAuthenticatedUsername();
            if (username == null) {
                return ResponseEntity.status(401).body("Unauthorized: No user found.");
            }

            CompanyDTO companyDTO = companyService.getByUser(username);

            if (companyDTO == null) {
                return ResponseEntity.status(404).body("Company not found");
            }

            return ResponseEntity.ok(companyDTO);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }

    @PutMapping("/updateCompany")
    public ResponseEntity<?> updateCompany(@Valid @RequestBody CompanyDTO companyDTO, BindingResult result) {
        try {
            if(result.hasErrors()) {
                List<String> errorMessage = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
                return ResponseEntity.badRequest().body(errorMessage);
            }
            String username = JwtUtil.getAuthenticatedUsername();
            if (username == null) {
                return ResponseEntity.status(401).body("Unauthorized: No user found.");
            }
            Company company = companyService.updateCompany(companyDTO, username);
            notificationService.sendNotificationToAdmin(company.getName() + " has been updated");
            return ResponseEntity.ok(company);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping(value = "/updateImages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateCompanyImages(
            @RequestParam("oldImages") String oldImagesJson,
            @RequestParam(value = "newImages", required = false) MultipartFile[] newImages
    ) {
        try {
            String username = JwtUtil.getAuthenticatedUsername();
            if (username == null) {
                return ResponseEntity.badRequest().body("Unauthorized: No user found.");
            }

            List<String> oldImages = new ObjectMapper().readValue(oldImagesJson, new TypeReference<>() {});

            companyService.updateCompanyImages(oldImages, newImages, username);

            return ResponseEntity.ok("Company images updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PutMapping(value = "/upload-logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadLogo(
            @RequestParam("file") MultipartFile file
    ) {
        try {
            String username = JwtUtil.getAuthenticatedUsername();
            if (username == null) {
                return ResponseEntity.badRequest().body("Unauthorized: No user found.");
            }
            companyService.uploadLogo(file, username);
            return ResponseEntity.ok("Upload logo successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/skills")
    public ResponseEntity<?> getAllSkillsForCompany() {
        try {
            String username = JwtUtil.getAuthenticatedUsername();
            if (username == null) {
                return ResponseEntity.badRequest().body("User not found");
            }

            CompanyDTO companyDTO = companyService.getByUser(username);
            if (companyDTO == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Company not found for user");
            }

            Set<Skill> skills = companyService.getAllSkillsForCompany(companyDTO.getId());

            if (skills.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No skills have been saved for this company.");
            }
            return ResponseEntity.ok(skills);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/addresses")
    public ResponseEntity<?> getAllAddressesForCompany() {
        try {
            String username = JwtUtil.getAuthenticatedUsername();
            if (username == null) {
                return ResponseEntity.badRequest().body("User not found");
            }

            CompanyDTO companyDTO = companyService.getByUser(username);
            if (companyDTO == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Company not found for user");
            }

            Set<Address> addresses = companyService.getAllAddressesForCompany(companyDTO.getId());

            if (addresses.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No addresses have been saved for this company.");
            }

            return ResponseEntity.ok(addresses);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "") String keyword
    ) {
        if (page < 0 || limit <= 0) {
            return ResponseEntity.badRequest().body("Page must be >= 0 and limit must be > 0.");
        }
        try {
            PageRequest pageRequest = PageRequest.of(
                    page, limit,
                    Sort.by("id").ascending()
            );
            Page<CompanyDTO> companyPage = companyService.searchCompanies(pageRequest, keyword);
            CompanyListResponse response = CompanyListResponse.builder()
                    .companies(companyPage.getContent())
                    .currentPage(page)
                    .pageSize(limit)
                    .totalPages(companyPage.getTotalPages())
                    .totalElements(companyPage.getTotalElements())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/total")
    public ResponseEntity<?> getTotalCompanies() {
        try {
            int totalCompanies = companyService.countCompanies();
            return ResponseEntity.ok(totalCompanies);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/related")
    public ResponseEntity<?> getRelatedCompanies(
            @RequestParam Long companyId
    ) {
        try {
            List<CompanyDTO> relatedCompanies = companyService.getRelatedCompanies(companyId);
            return ResponseEntity.ok(relatedCompanies);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verify-license")
    public ResponseEntity<?> verifyBusinessLicense (
            @RequestParam("license") MultipartFile license
    ) {
        String username = JwtUtil.getAuthenticatedUsername();

        if (username == null) {
            return ResponseEntity.status(401).body("Unauthorized: No user found.");
        }

        if (!fileUtil.isImageFormatValid(license)) {
            return ResponseEntity.badRequest().body("Invalid image format");
        }
        try {
            String filename = fileUtil.storeFile(license);
            companyService.updateLicense(filename, username);
            return ResponseEntity.ok("License uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error occurred while saving file: " + e.getMessage());
        }
    }

    @GetMapping("/check-license")
    public ResponseEntity<?> checkLicense() {
        String username = JwtUtil.getAuthenticatedUsername();

        if (username == null) {
            return ResponseEntity.status(401).body("Unauthorized: No user found.");
        }

        try {
            CompanyDTO companyDTO = companyService.getByUser(username);
            if (companyDTO.getBusinessLicense() == null) {
                return ResponseEntity.ok("License not uploaded");
            }
            return ResponseEntity.ok("License uploaded");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/company-status")
    public ResponseEntity<?> getCompanyStatus() {
        String username = JwtUtil.getAuthenticatedUsername();

        if (username == null) {
            return ResponseEntity.status(401).body("Unauthorized: No user found.");
        }

        try {
            CompanyDTO companyDTO = companyService.getByUser(username);
            return ResponseEntity.ok(companyDTO.getCompanyStatus());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error occurred: " + e.getMessage());
        }
    }

    @PutMapping("/approve-company")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveCompany(@RequestParam Long companyId) {
        try {
            companyService.approveCompany(companyId);
            return ResponseEntity.ok("Company approved successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error occurred: " + e.getMessage());
        }
    }

    @PutMapping("/reject-company")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectCompany(@RequestParam Long companyId, @RequestParam String reason) {
        try {
            companyService.rejectCompany(companyId, reason);
            return ResponseEntity.ok("Company rejected successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error occurred: " + e.getMessage());
        }
    }
}
