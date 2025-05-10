package com.hcmute.devhire.components;

import com.hcmute.devhire.entities.Company;
import com.hcmute.devhire.entities.CompanyReview;
import com.hcmute.devhire.entities.Job;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CompanyScoreCalculator {

    public double calculateScore(Company company,
                                 List<CompanyReview> reviews,
                                 List<Job> jobs) {

        double reviewScore = calculateReviewScore(reviews);
        double scaleScore = calculateScaleScore(company.getScale());
        double jobScore = calculateJobScore(jobs);
        double salaryScore = calculateSalaryScore(jobs);

        double totalScore = reviewScore * 0.4 + scaleScore * 0.2 + jobScore * 0.2 + salaryScore * 0.2;
        return totalScore * 100; // convert to 0–100 scale
    }

    private double calculateReviewScore(List<CompanyReview> reviews) {
        if (reviews == null || reviews.isEmpty()) return 0.5;
        double avg = reviews.stream().mapToInt(CompanyReview::getRating).average().orElse(0);
        return avg / 5.0; // normalize to 0–1
    }

    private double calculateScaleScore(Integer scale) {
        if (scale == null) return 0.5;
        return Math.min(Math.log(scale + 1) / Math.log(1000), 1.0); // normalized
    }

    private double calculateJobScore(List<Job> jobs) {
        if (jobs == null || jobs.isEmpty()) return 0.0;
        long activeJobs = jobs.stream()
                .filter(job -> job.getStatus() != null &&
                        (job.getStatus().name().equals("OPEN") || job.getStatus().name().equals("HOT")))
                .count();
        return Math.min(activeJobs / 10.0, 1.0);
    }

    private double calculateSalaryScore(List<Job> jobs) {
        if (jobs == null || jobs.isEmpty()) return 0.5;

        double avgSalary = jobs.stream()
                .filter(j -> j.getSalaryStart() != null && j.getSalaryEnd() != null)
                .mapToDouble(j -> (j.getSalaryStart() + j.getSalaryEnd()) / 2)
                .average()
                .orElse(0);

        return Math.min(avgSalary / 100_000_000, 1.0);
    }
}
