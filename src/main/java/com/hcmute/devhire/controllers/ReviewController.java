package com.hcmute.devhire.controllers;

import com.hcmute.devhire.DTOs.CompanyReviewDTO;
import com.hcmute.devhire.entities.CompanyReview;
import com.hcmute.devhire.responses.CompanyReviewListResponse;
import com.hcmute.devhire.services.ICompanyReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ICompanyReviewService companyReviewService;

    @PostMapping("/{companyId}")
    public ResponseEntity<?> addReview(
            @PathVariable Long companyId,
            @RequestBody @Valid CompanyReviewDTO companyReviewDTO,
            BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Invalid input");
        }

        String username = getAuthenticatedUsername();
        try {
            CompanyReview savedReview = companyReviewService.addReview(companyId, username, companyReviewDTO);
            return ResponseEntity.ok(savedReview);
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


    @GetMapping("/{companyId}")
    public ResponseEntity<?> getReviews(
            @PathVariable Long companyId,
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
            Page<CompanyReviewDTO> companyReviewDTOPage = companyReviewService.getReviewsByCompanyId(pageRequest, companyId);
            CompanyReviewListResponse response = CompanyReviewListResponse.builder()
                    .companyReviews(companyReviewDTOPage.getContent())
                    .currentPage(page)
                    .pageSize(limit)
                    .totalPages(companyReviewDTOPage.getTotalPages())
                    .totalElements(companyReviewDTOPage.getTotalElements())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
