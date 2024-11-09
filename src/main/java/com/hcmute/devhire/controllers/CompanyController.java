package com.hcmute.devhire.controllers;

import com.hcmute.devhire.DTOs.CompanyDTO;
import com.hcmute.devhire.entities.Address;
import com.hcmute.devhire.entities.Company;
import com.hcmute.devhire.entities.Skill;
import com.hcmute.devhire.responses.CompanyListResponse;
import com.hcmute.devhire.services.CompanyService;
import com.hcmute.devhire.services.ICompanyService;
import com.hcmute.devhire.services.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/companies")
public class CompanyController {
    private final ICompanyService companyService;
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
                    Sort.by("id").ascending()
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
            String username = getAuthenticatedUsername();
            if (username == null) {
                return ResponseEntity.badRequest().body("User not found");
            }
            Company newCompany = companyService.createCompany(companyDTO, username);
            return ResponseEntity.ok(newCompany);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return null;
    }

    @GetMapping("/profile")
    public  ResponseEntity<?> getByCreated(){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = null;

            if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
                username = userDetails.getUsername();
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
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
            String username = getAuthenticatedUsername();
            if (username == null) {
                return ResponseEntity.status(401).body("Unauthorized: No user found.");
            }
            Company company = companyService.updateCompany(companyDTO, username);
            return ResponseEntity.ok(company);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping(value = "/upload-logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadLogo(
            @RequestParam("file") MultipartFile file
    ) {
        try {
            String username = getAuthenticatedUsername();
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
            String username = getAuthenticatedUsername();
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
            String username = getAuthenticatedUsername();
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
}
