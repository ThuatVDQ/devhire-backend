package com.hcmute.devhire.services;

import com.hcmute.devhire.responses.CountPerJobResponse;
import com.hcmute.devhire.responses.DashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AdminService implements IAdminService {
    private final IJobService jobService;
    private final IUserService userService;
    private final ICompanyService companyService;
    private final IJobApplicationService jobApplicationService;
    @Override
    public DashboardResponse getDashboardData() throws Exception {
        LocalDate currentDate = LocalDate.now();
        LocalDate previousMonthDate = currentDate.minusMonths(1);

        int previousMonth = previousMonthDate.getMonthValue();
        int previousYear = previousMonthDate.getYear();
        int currentMonth = currentDate.getMonthValue();

        int currentYear = currentDate.getYear();

        int countUser = userService.countUsers();
        int countJob = jobService.countJobs();
        int countCompany = companyService.countCompanies();

        double growthUser = calculateGrowth(userService.countUsersMonthly(currentMonth, currentYear), userService.countUsersMonthly(previousMonth, previousYear));
        double growthJob = calculateGrowth(jobService.countJobsMonthly(currentMonth, currentYear), jobService.countJobsMonthly(previousMonth, previousYear));
        double growthCompany = calculateGrowth(companyService.countCompaniesMonthly(currentMonth, currentYear), companyService.countCompaniesMonthly(previousMonth, previousYear));

        return DashboardResponse.builder()
                .users(DashboardResponse.Stats.builder().count(countUser).growth(growthUser).build())
                .jobs(DashboardResponse.Stats.builder().count(countJob).growth(growthJob).build())
                .companies(DashboardResponse.Stats.builder().count(countCompany).growth(growthCompany).build())
                .build();
    }

    private double calculateGrowth(int current, int previous) {
        double growth;
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }else {
            growth = ((double) (current - previous) / previous) * 100;
        }

        BigDecimal roundedGrowth = new BigDecimal(growth).setScale(2, RoundingMode.HALF_UP);
        return roundedGrowth.doubleValue();
    }
}
