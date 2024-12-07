package com.hcmute.devhire.controllers;

import com.hcmute.devhire.DTOs.JobDTO;
import com.hcmute.devhire.DTOs.UserDTO;
import com.hcmute.devhire.components.JwtUtil;
import com.hcmute.devhire.entities.Role;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.responses.*;
import com.hcmute.devhire.services.IAdminService;
import com.hcmute.devhire.services.IJobApplicationService;
import com.hcmute.devhire.services.IJobService;
import com.hcmute.devhire.services.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/getAllUsers")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) String status,
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
            if (!isUserAdmin()) {
                return ResponseEntity.badRequest()
                        .body("Error: You are not an admin");
            }
            Page<UserResponse> users = userService.getAllUsers(pageRequest, roleId, status);
            UserListResponse userListResponse = UserListResponse.builder()
                    .users(users.getContent())
                    .totalPages(users.getTotalPages())
                    .totalElements(users.getTotalElements())
                    .currentPage(page)
                    .pageSize(limit)
                    .build();
            return ResponseEntity.ok(userListResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/getAllJobs")
    public ResponseEntity<?> getAllJobs(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
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
            if (!isUserAdmin()) {
                return ResponseEntity.badRequest()
                        .body("Error: You are not an admin");
            }
            Page<JobDTO> jobs = jobService.getAllJobsAdmin(pageRequest, status, type, null);
            JobListResponse jobListResponse = JobListResponse.builder()
                    .jobs(jobs.getContent())
                    .totalPages(jobs.getTotalPages())
                    .totalElements(jobs.getTotalElements())
                    .currentPage(page)
                    .pageSize(limit)
                    .build();
            return ResponseEntity.ok(jobListResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/banUser")
    public ResponseEntity<?> banUser(@RequestParam Long userId) {
        try {
            if (!isUserAdmin()) {
                return ResponseEntity.badRequest()
                        .body("Error: You are not an admin");
            }
            userService.changeStatusUser("INACTIVE",userId);
            return ResponseEntity.ok("User banned successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/unbanUser")
    public ResponseEntity<?> unbanUser(@RequestParam Long userId) {
        try {
            if (!isUserAdmin()) {
                return ResponseEntity.badRequest()
                        .body("Error: You are not an admin");
            }
            userService.changeStatusUser("ACTIVE",userId);
            return ResponseEntity.ok("User unbanned successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            if (!isUserAdmin()) {
                return ResponseEntity.badRequest()
                        .body("Error: You are not an admin");
            }
            UserDTO user = userService.findUserDTOById(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/latestUsers")
    public ResponseEntity<?> getLatestUsers() {
        try {
            if (!isUserAdmin()) {
                return ResponseEntity.badRequest()
                        .body("Error: You are not an admin");
            }
            List<UserDTO> users = userService.get5LatestUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/latestJobs")
    public ResponseEntity<?> getLatestJobs() {
        try {
            if (!isUserAdmin()) {
                return ResponseEntity.badRequest()
                        .body("Error: You are not an admin");
            }
            List<JobDTO> jobs = jobService.get5LatestJobs();
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    public boolean isUserAdmin() throws Exception {
        String username = JwtUtil.getAuthenticatedUsername();

        if (username == null) {
            return false;
        }
        User user = userService.findByUserName(username);
        return user.getRole() != null && Role.ADMIN.equals(user.getRole().getName().toUpperCase());
    }
}
