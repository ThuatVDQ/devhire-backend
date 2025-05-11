package com.hcmute.devhire.controllers;

import com.hcmute.devhire.components.JwtUtil;
import com.hcmute.devhire.exceptions.DataNotFoundException;
import com.hcmute.devhire.responses.CompanyScoreResponse;
import com.hcmute.devhire.services.ICompanyScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scoring")
@RequiredArgsConstructor
public class CompanyScoringController {

    private final ICompanyScoringService companyScoringService;

    @GetMapping("/company")
    public ResponseEntity<CompanyScoreResponse> scoreCompanyWithJwt() throws DataNotFoundException {
        String username = JwtUtil.getAuthenticatedUsername();
        if (username == null) {
            return ResponseEntity.badRequest().body(null);
        }
        CompanyScoreResponse response = companyScoringService.calculateCompanyScore(username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<CompanyScoreResponse> scoreCompany(
            @PathVariable Long companyId) throws DataNotFoundException {

        CompanyScoreResponse response = companyScoringService.calculateCompanyScore(companyId);
        return ResponseEntity.ok(response);
    }


}
