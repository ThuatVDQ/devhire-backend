package com.hcmute.devhire.controllers;

import com.hcmute.devhire.services.IJobNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/job-notification")
public class JobNotificationController {
    private final IJobNotificationService jobNotificationService;

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(@RequestParam String email) {
        String response = jobNotificationService.subscribe(email);
        return ResponseEntity.ok(response);
    }
}
