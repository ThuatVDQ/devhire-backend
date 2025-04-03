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
    public String subscribe(String email) throws Exception {
        if (subscriptionRepository.findByEmail(email).isPresent()) {
            throw new Exception("Email đã đăng ký nhận thông báo.");
        }

        JobNotificationSubscription subscription = new JobNotificationSubscription();
        subscription.setEmail(email);
        subscription.setCreatedAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);

        return "Đăng ký nhận thông báo thành công!";
    }

    @Override
    public String unsubscribe(String email) throws Exception {
        JobNotificationSubscription subscription = subscriptionRepository.findByEmail(email).orElse(null);
        if (subscription != null) {
            subscription.setStatus(false);
            subscriptionRepository.save(subscription);
            return "Hủy đăng ký nhận thông báo thành công!";
        }
        throw new Exception("Email không tồn tại trong danh sách đăng ký.");
    }

    @Override
    public boolean checkSubscribed(String email) throws Exception {
        JobNotificationSubscription subscription = subscriptionRepository.findByEmail(email).orElse(null);
        if (subscription != null && subscription.isStatus()) {
            return true;
        }
        throw new Exception("Email không tồn tại trong danh sách đăng ký.");
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
