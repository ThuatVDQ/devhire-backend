package com.hcmute.devhire.components;

import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.repositories.JobRepository;
import com.hcmute.devhire.services.IEmailService;
import com.hcmute.devhire.services.INotificationService;
import com.hcmute.devhire.utils.JobStatus;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JobStatusUpdater {
    private final JobRepository jobRepository;
    private final IEmailService emailService;
    private final INotificationService notificationService;

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
}
