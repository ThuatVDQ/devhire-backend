package com.hcmute.devhire.components;

import com.hcmute.devhire.entities.Company;
import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.entities.MemberVip;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.repositories.CompanyRepository;
import com.hcmute.devhire.repositories.JobRepository;
import com.hcmute.devhire.repositories.MemberVipRepository;
import com.hcmute.devhire.services.IEmailService;
import com.hcmute.devhire.services.INotificationService;
import com.hcmute.devhire.utils.JobStatus;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JobStatusUpdater {
    private final JobRepository jobRepository;
    private final IEmailService emailService;
    private final INotificationService notificationService;
    private final MemberVipRepository memberVipRepository;
    private final CompanyRepository companyRepository;

    @Scheduled(fixedRate = 86400000)
    public void updateJobStatus() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        List<JobStatus> statuses = List.of(JobStatus.OPEN, JobStatus.HOT);
        List<Job> jobsToClose = jobRepository.findByDeadlineBeforeAndStatusIn(now, statuses);

        for (Job job : jobsToClose) {
            job.setStatus(JobStatus.CLOSED);
            jobRepository.save(job);
            emailService.sendEmail(job.getCompany().getCreatedBy().getUsername(), "Job " + job.getTitle() + " has been closed", "Your job has been closed because the deadline has passed.");
            notificationService.createAndSendNotification("Your job " + job.getTitle() + " has been closed because the deadline has passed.", job.getCompany().getCreatedBy().getUsername());
        }
    }

    @Scheduled(cron = "0 0 1 * * ?") // 1AM mỗi ngày
    public void clearExpiredHighlightJobs() {
        List<Job> jobs = jobRepository.findByHighlightEndTimeBefore(LocalDateTime.now());
        for (Job job : jobs) {
            job.setHighlightEndTime(null);
        }
        jobRepository.saveAll(jobs);
    }

    @Scheduled(fixedRate = 86400000) // 1 ngày
    @Transactional
    public void removeHotBadgeWhenVipExpired() {
        LocalDateTime now = LocalDateTime.now();

        // Tìm các MemberVip đã hết hạn
        List<MemberVip> expiredVips = memberVipRepository.findByExpireDayBefore(new Date());

        for (MemberVip memberVip : expiredVips) {
            User user = memberVip.getUser();
            Company company = companyRepository.findByUser(String.valueOf(user));

            if (company != null) {
                List<Job> jobs = jobRepository.findByCompanyIdAndStatus(company.getId(), JobStatus.HOT);
                for (Job job : jobs) {
                    job.setStatus(JobStatus.OPEN); // hoặc ACTIVE tùy hệ thống bạn định nghĩa
                    jobRepository.save(job);
                }
            }
        }
    }
}
