package com.hcmute.devhire.services;

import com.hcmute.devhire.entities.Company;
import com.hcmute.devhire.entities.CompanyReview;
import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.exceptions.DataNotFoundException;
import com.hcmute.devhire.repositories.CompanyRepository;
import com.hcmute.devhire.repositories.CompanyReviewRepository;
import com.hcmute.devhire.repositories.JobRepository;
import com.hcmute.devhire.responses.CompanyScoreResponse;
import com.hcmute.devhire.utils.JobStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompanyScoringService implements ICompanyScoringService {
    private final CompanyRepository companyRepository;
    private final CompanyReviewRepository companyReviewRepository;
    private final JobRepository jobRepository;
    @Override
    public CompanyScoreResponse calculateCompanyScore(Long companyId) throws DataNotFoundException {
        Optional<Company> companyOpt = companyRepository.findById(companyId);
        if (companyOpt.isEmpty()) {
            throw new DataNotFoundException("Company not found");
        }

        Company company = companyOpt.get();
        CompanyScoreResponse response = new CompanyScoreResponse();
        response.setCompanyId(companyId);
        response.setCompanyName(company.getName());

        // Tính điểm tổng
        double totalScore = 0;
        Map<String, Double> scoreDetails = new HashMap<>();

        // 1. Điểm đánh giá (40%)
        double reviewScore = calculateReviewScore(companyId);
        scoreDetails.put("reviews", reviewScore);
        totalScore += reviewScore * 0.4;

        // 2. Điểm quy mô (20%)
        double scaleScore = calculateScaleScore(company.getScale());
        scoreDetails.put("scale", scaleScore);
        totalScore += scaleScore * 0.2;

        // 3. Điểm công việc (20%)
        double jobScore = calculateJobScore(companyId);
        scoreDetails.put("jobs", jobScore);
        totalScore += jobScore * 0.2;

        // 4. Điểm lương (20%)
        double salaryScore = calculateSalaryScore(companyId);
        scoreDetails.put("salary", salaryScore);
        totalScore += salaryScore * 0.2;


        response.setTotalScore(totalScore * 100); // Chuyển về thang 100
        response.setScoreDetails(scoreDetails);
        response.setStarRating(reviewScore * 5); // Chuyển về thang 5 sao

        return response;
    }

    private double calculateReviewScore(Long companyId) {
        List<CompanyReview> reviews = companyReviewRepository.findByCompanyId(companyId);
        if (reviews.isEmpty()) {
            return 0.5; // Điểm trung bình nếu không có review
        }

        double avgRating = reviews.stream()
                .mapToInt(CompanyReview::getRating)
                .average()
                .orElse(0);

        return avgRating / 5.0; // Chuẩn hóa về thang 0-1
    }

    private double calculateScaleScore(Integer scale) {
        if (scale == null) return 0.5;

        // Log scale để không quá thiên vị công ty lớn
        return Math.min(Math.log(scale + 1) / Math.log(1000), 1.0);
    }

    private double calculateJobScore(Long companyId) {
        List<JobStatus> statuses = List.of(JobStatus.OPEN, JobStatus.HOT);
        long activeJobs = jobRepository.countByCompanyIdAndStatuses(companyId, statuses);
        return Math.min(activeJobs / 10.0, 1.0); // Tối đa 10 job = điểm 1.0
    }

    private double calculateSalaryScore(Long companyId) {
        List<Job> jobs = jobRepository.findByCompanyId(companyId);
        if (jobs.isEmpty()) return 0.5;

        double avgSalary = jobs.stream()
                .filter(j -> j.getSalaryStart() != null && j.getSalaryEnd() != null)
                .mapToDouble(j -> (j.getSalaryStart() + j.getSalaryEnd()) / 2)
                .average()
                .orElse(0);

        // Chuẩn hóa về thang điểm (giả sử mức lương tối đa 100 triệu)
        return Math.min(avgSalary / 100_000_000, 1.0);
    }
}
