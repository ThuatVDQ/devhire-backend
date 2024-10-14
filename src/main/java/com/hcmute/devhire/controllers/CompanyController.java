package com.hcmute.devhire.controllers;

import com.hcmute.devhire.DTOs.CompanyDTO;
import com.hcmute.devhire.entities.Company;
import com.hcmute.devhire.responses.CompanyListResponse;
import com.hcmute.devhire.services.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyService companyService;
    @GetMapping("")
    public ResponseEntity<?> getAllCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        PageRequest pageRequest = PageRequest.of(
                page, limit,
                Sort.by("id").ascending()
        );
        Page<Company> companyPage = companyService.getAllCompanies(pageRequest);
        int totalPages = companyPage.getTotalPages();
        List<Company> companies = companyPage.getContent();
        return ResponseEntity.ok(Collections.singletonList(CompanyListResponse
                .builder()
                .companies(companies)
                .totalPages(totalPages)
                .build()));
    }

    @PostMapping("")
    public ResponseEntity<?> createCompany(@RequestBody CompanyDTO companyDTO) {
        Company newCompany = companyService.createCompany(companyDTO);
        return ResponseEntity.ok(newCompany);
    }
}
