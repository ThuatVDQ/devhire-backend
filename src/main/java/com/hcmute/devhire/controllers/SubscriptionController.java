package com.hcmute.devhire.controllers;

import com.hcmute.devhire.DTOs.SubscriptionDTO;
import com.hcmute.devhire.entities.Subscription;
import com.hcmute.devhire.services.ISubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscription")
public class SubscriptionController {
    private final ISubscriptionService subscriptionService;

    @GetMapping("")
    public ResponseEntity<?> getAllSubscriptions(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit

    ) {
        try {
            PageRequest pageRequest = PageRequest.of(
                    page, limit,
                    Sort.by("id").ascending()
            );
            Page<SubscriptionDTO> subscriptions = subscriptionService.getAllSubscriptions(pageRequest);
            return ResponseEntity.ok(subscriptions.getContent());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addSubscription(@RequestBody SubscriptionDTO subscriptionDTO) {
        try {
            Subscription subscription = subscriptionService.addSubscription(subscriptionDTO);
            return ResponseEntity.ok(subscription);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteSubscription(@RequestParam("id") Long id) {
        try {
            subscriptionService.deleteSubscription(id);
            return ResponseEntity.ok("Subscription deleted");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/active")
    public ResponseEntity<?> activeSubscription(@RequestParam("id") Long id) {
        try {
            subscriptionService.activeSubscription(id);
            return ResponseEntity.ok("Subscription activated");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/isExist")
    public ResponseEntity<?> isSubscriptionExist(@RequestParam("name") String name) {
        try {
            boolean isExist = subscriptionService.isSubscriptionExist(name);
            return ResponseEntity.ok(isExist);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }
}
