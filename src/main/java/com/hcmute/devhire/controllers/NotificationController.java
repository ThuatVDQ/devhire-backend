package com.hcmute.devhire.controllers;

import com.hcmute.devhire.DTOs.NotificationDTO;
import com.hcmute.devhire.entities.Notification;
import com.hcmute.devhire.services.INotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {
    private final INotificationService notificationService;

    @GetMapping("/")
    public ResponseEntity<?> getUserNotifications() throws Exception {
        String username = getUsername();
        if (username == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        try {
            List<Notification> notifications = notificationService.getUserNotifications(username);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId) throws Exception {
        try {
            notificationService.markAsRead(notificationId);
            return ResponseEntity.ok("Notification marked as read.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }

    }

    @PostMapping("/create")
    public ResponseEntity<?> createNotification(@Valid @RequestBody NotificationDTO notificationDTO) {
        try {
            notificationService.createAndSendNotification(notificationDTO.getMessage(), notificationDTO.getUsername());
            return ResponseEntity.ok("Notification created successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long notificationId) {
        try {
            notificationService.deleteNotification(notificationId);
            return ResponseEntity.ok("Notification deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("An error occurred: " + e.getMessage());
        }
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead() {
        String username = getUsername();
        if (username == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        try {
            notificationService.markAllAsRead(username);
            return ResponseEntity.ok("All notifications marked as read.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadNotificationCount() {
        String username = getUsername();
        if (username == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        try {
            long count = notificationService.countUnreadNotifications(username);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }

    private String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return null;
    }
}
