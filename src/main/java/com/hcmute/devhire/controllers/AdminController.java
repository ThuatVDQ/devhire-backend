package com.hcmute.devhire.controllers;

import com.hcmute.devhire.entities.Role;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.responses.DashboardResponse;
import com.hcmute.devhire.services.IAdminService;
import com.hcmute.devhire.services.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final IAdminService adminService;
    private final IUserService userService;
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
