package com.hcmute.devhire.controllers;

import com.hcmute.devhire.entities.Role;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.responses.CountPerJobResponse;
import com.hcmute.devhire.responses.DashboardResponse;
import com.hcmute.devhire.responses.MonthlyCountResponse;
import com.hcmute.devhire.services.IAdminService;
import com.hcmute.devhire.services.IJobApplicationService;
import com.hcmute.devhire.services.IJobService;
import com.hcmute.devhire.services.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {
    private final IJobApplicationService jobApplicationService;
    private final IAdminService adminService;
    private final IUserService userService;
    private final IJobService jobService;
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardData() {
        try {
            if (!isUserAdmin()) {
                return ResponseEntity.badRequest()
                        .body("Error: You are not an admin");
            }
            DashboardResponse dashboardResponse = adminService.getDashboardData();
            return ResponseEntity.ok(dashboardResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/applicationsPerJob")
    public ResponseEntity<?> getApplicationsPerJob() {
        try {
            if (!isUserAdmin()) {
                return ResponseEntity.badRequest()
                        .body("Error: You are not an admin");
            }
            List<CountPerJobResponse> countPerJobResponses = jobApplicationService.countJobApplicationPerJob();
            return ResponseEntity.ok(countPerJobResponses);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/monthlyApplications")
    public ResponseEntity<?> getMonthlyApplications(@RequestParam("year") int year) {
        try {
            if (!isUserAdmin()) {
                return ResponseEntity.badRequest()
                        .body("Error: You are not an admin");
            }
            List<MonthlyCountResponse> monthlyApplicationCountResponses = jobApplicationService.countJobApplicationByMonth(year);
            return ResponseEntity.ok(monthlyApplicationCountResponses);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/monthlyJobs")
    public ResponseEntity<?> getMonthlyJobs(@RequestParam("year") int year) {
        try {
            if (!isUserAdmin()) {
                return ResponseEntity.badRequest()
                        .body("Error: You are not an admin");
            }
            List<MonthlyCountResponse> monthlyApplicationCountResponses = jobService.countJobsByMonth(year);
            return ResponseEntity.ok(monthlyApplicationCountResponses);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    public boolean isUserAdmin() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = null;

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        }

        if (username == null) {
            return false;
        }
        User user = userService.findByUserName(username);
        return user.getRole() != null && Role.ADMIN.equals(user.getRole().getName().toUpperCase());
    }
}
