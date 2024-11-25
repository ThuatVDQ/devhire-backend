package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.CompanyDTO;
import com.hcmute.devhire.DTOs.CompanyReviewDTO;
import com.hcmute.devhire.entities.Company;
import com.hcmute.devhire.entities.CompanyReview;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.repositories.CompanyReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyReviewService implements ICompanyReviewService {
    private final CompanyReviewRepository companyReviewRepository;
    private final ICompanyService companyService;
    private final IUserService userService;
    private final INotificationService notificationService;
    @Override
    public CompanyReview addReview(Long companyId, String username, CompanyReviewDTO companyReviewDTO) throws Exception {
        Company company = companyService.getCompanyByID(companyId);

        User user = userService.findByUserName(username);

        CompanyReview companyReview = CompanyReview.builder()
                .company(company)
                .user(user)
                .rating(companyReviewDTO.getRating())
                .comment(companyReviewDTO.getComment())
                .build();
        notificationService.createAndSendNotification("User " + user.getFullName() + " just reviewed your company", company.getCreatedBy().getUsername());
        notificationService.sendNotificationToAdmin("User " + user.getFullName() + " just reviewed company " + company.getName());
        return companyReviewRepository.save(companyReview);
    }

    public CompanyReviewDTO convertToDTO(CompanyReview companyReview) {
        return CompanyReviewDTO.builder()
                .rating(companyReview.getRating())
                .comment(companyReview.getComment())
                .fullName(companyReview.getUser().getFullName())
                .build();
    }

    @Override
    public Page<CompanyReviewDTO> getReviewsByCompanyId(PageRequest pageRequest, Long companyId) throws Exception {
        Company company = companyService.getCompanyByID(companyId);
        Page<CompanyReview> companyReviews = companyReviewRepository.findByCompanyId(pageRequest, companyId);
        return companyReviews.map(this::convertToDTO);
    }
}
