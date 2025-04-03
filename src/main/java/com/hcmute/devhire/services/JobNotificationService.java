package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.JobDTO;
import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.entities.JobNotificationSubscription;
import com.hcmute.devhire.repositories.JobNotificationSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobNotificationService implements IJobNotificationService {
    private final JobNotificationSubscriptionRepository subscriptionRepository;
    private final IEmailService emailService;
    private final IJobService jobService;
    private final JobNotificationSubscriptionRepository jobNotificationSubscriptionRepository;

    @Override
    public String subscribe(String email) {
        if (subscriptionRepository.findByEmail(email).isPresent()) {
            return "Email đã đăng ký nhận thông báo.";
        }

        JobNotificationSubscription subscription = new JobNotificationSubscription();
        subscription.setEmail(email);
        subscription.setCreatedAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);

        return "Đăng ký nhận thông báo thành công!";
    }

    @Override
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendJobNotifications() {
        List<JobNotificationSubscription> list = jobNotificationSubscriptionRepository.findAll();

        list.forEach(subscription -> {
            String email = subscription.getEmail();
            List<JobDTO> newJobs = null;
            try {
                newJobs = jobService.getNewJobsForUser(email);
                if (newJobs != null && !newJobs.isEmpty()) {
                    emailService.sendEmail(email, "New Job Notifications", newJobs);
                }
            } catch (Exception e) {
                System.out.println("Error fetching new jobs for email: " + email);
            }
        });
    }
}
