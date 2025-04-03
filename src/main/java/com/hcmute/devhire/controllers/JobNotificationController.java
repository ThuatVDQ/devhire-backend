package com.hcmute.devhire.controllers;

import com.hcmute.devhire.components.JwtUtil;
import com.hcmute.devhire.services.IJobNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/job-notification")
public class JobNotificationController {
    private final IJobNotificationService jobNotificationService;

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe() {
        String username = JwtUtil.getAuthenticatedUsername();
        try {
            String response = jobNotificationService.subscribe(username);
            jobNotificationService.sendJobNotifications();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<String> unsubscribe() {
        String username = JwtUtil.getAuthenticatedUsername();
        try {
            String response = jobNotificationService.unsubscribe(username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/check-subscribed")
    public ResponseEntity<?> checkSubscribed() {
        String username = JwtUtil.getAuthenticatedUsername();
        try {
            boolean isSubscribed = jobNotificationService.checkSubscribed(username);
            return ResponseEntity.ok(isSubscribed);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
