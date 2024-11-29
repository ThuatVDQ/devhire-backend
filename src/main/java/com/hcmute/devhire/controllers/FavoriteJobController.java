package com.hcmute.devhire.controllers;

import com.hcmute.devhire.components.JwtUtil;
import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.responses.JobListResponse;
import com.hcmute.devhire.services.IFavoriteJobService;
import com.hcmute.devhire.services.IJobService;
import com.hcmute.devhire.services.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorite-job")
public class FavoriteJobController {
    private final IFavoriteJobService favoriteJobService;
    private final IUserService userService;
    private final IJobService jobService;

    @GetMapping("/favorite")
    public ResponseEntity<?> getFavoriteJobs() {
        try {
            String username = JwtUtil.getAuthenticatedUsername();
            if (username == null) {
                return ResponseEntity.badRequest().body("Unauthorized: No user found.");
            }

            User user = userService.findByUserName(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }
            JobListResponse jobListResponse = jobService.getFavoriteJobs(user);
            return ResponseEntity.ok(jobListResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("An error occurred: " + e.getMessage());
        }
    }
    @DeleteMapping("/remove")
    public ResponseEntity<String> removeFavorite(@RequestParam Long jobId) throws Exception {
        try {
            String username = JwtUtil.getAuthenticatedUsername();
            if (username == null) {
                return ResponseEntity.status(401).body("Unauthorized: No user found.");
            }

            User user = userService.findByUserName(username);
            Job job = jobService.findById(jobId);

            if (user == null || job == null) {
                return ResponseEntity.badRequest().body("User or Job not found");
            }

            favoriteJobService.removeFavorite(user, job);
            return ResponseEntity.ok("Job removed from favorites");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("An error occurred: " + e.getMessage());
        }
    }

}
