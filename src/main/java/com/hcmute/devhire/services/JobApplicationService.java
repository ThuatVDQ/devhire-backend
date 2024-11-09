package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.EmailRequestDTO;
import com.hcmute.devhire.DTOs.JobApplicationDTO;
import com.hcmute.devhire.entities.CV;
import com.hcmute.devhire.entities.Job;
import com.hcmute.devhire.entities.JobApplication;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.exceptions.DataNotFoundException;
import com.hcmute.devhire.repositories.CVRepository;
import com.hcmute.devhire.repositories.JobApplicationRepository;
import com.hcmute.devhire.repositories.JobRepository;
import com.hcmute.devhire.repositories.UserRepository;
import com.hcmute.devhire.utils.JobApplicationStatus;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobApplicationService implements IJobApplicationService{
    private final JobApplicationRepository jobApplicationRepository;
    private final IEmailService emailService;
    @Override
    public JobApplicationDTO getJobApplication(Long jobApplicationId) throws DataNotFoundException {
        JobApplication jobApplication = jobApplicationRepository.findById(jobApplicationId)
                .orElseThrow(() ->
                        new DataNotFoundException("Job application not found with id: " + jobApplicationId));
        return JobApplicationDTO.builder()
                .status(jobApplication.getStatus().name())
                .jobId(jobApplication.getJob().getId())
                .userId(jobApplication.getUser().getId())
                .cvId(jobApplication.getCv().getId())
                .build();
    }

    @Override
    public List<String> getAllCvPathsByJobId(Long jobId) {
        List<JobApplication> jobApplications = jobApplicationRepository.findByJobId(jobId);
        return jobApplications.stream()
                .map(JobApplication::getCv)
                .filter(Objects::nonNull)
                .map(CV::getCvUrl)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<JobApplication> findApplicationByJobIdAndUserId(Long jobId, Long userId) {
        JobApplication jobApplications = jobApplicationRepository.findByJobIdAndUserId(jobId, userId);
        if (jobApplications != null) {
            return Optional.of(jobApplications);
        }
        return Optional.empty();
    }

    @Override
    public void updateJobApplication(JobApplication jobApplication) {
        jobApplicationRepository.save(jobApplication);
    }

    @Override
    public void seenJobApplication(Long jobApplicationId) {
        try {
            JobApplication jobApplication = jobApplicationRepository.findById(jobApplicationId).get();
            jobApplication.setStatus(JobApplicationStatus.SEEN);
            jobApplicationRepository.save(jobApplication);
        } catch (Exception e) {
            throw new RuntimeException("Job application not found with id: " + jobApplicationId);
        }
    }

    @Override
    public void seenAllJobApplication(Long jobId) {
        List<JobApplication> jobApplications = jobApplicationRepository.findByJobId(jobId);
        if (jobApplications.isEmpty()) {
            throw new RuntimeException("Job applications not found");
        }
        jobApplications.forEach(jobApplication -> {
            if (jobApplication.getStatus().equals(JobApplicationStatus.IN_PROGRESS)) {
                jobApplication.setStatus(JobApplicationStatus.SEEN);
                jobApplicationRepository.save(jobApplication);
            }
        });
    }

    @Override
    public void rejectJobApplication(Long jobApplicationId) {
        try {
            JobApplication jobApplication = jobApplicationRepository.findById(jobApplicationId).get();
            jobApplication.setStatus(JobApplicationStatus.REJECTED);
            jobApplicationRepository.save(jobApplication);
        } catch (Exception e) {
            throw new RuntimeException("Job application not found with id: " + jobApplicationId);
        }
    }

    @Override
    public void approveJobApplication(Long jobApplicationId) {
        try {
            JobApplication jobApplication = jobApplicationRepository.findById(jobApplicationId).get();
            jobApplication.setStatus(JobApplicationStatus.ACCEPTED);
            jobApplicationRepository.save(jobApplication);
        } catch (Exception e) {
            throw new RuntimeException("Job application not found with id: " + jobApplicationId);
        }
    }

    @Override
    public void sendEmailToApplicant(EmailRequestDTO emailRequestDTO) {

        String htmlMessage = String.format("""
    <!DOCTYPE html>
    <html lang='en'>
    <head>
        <meta charset='UTF-8'>
        <meta name='viewport' content='width=device-width, initial-scale=1.0'>
        <title>CV Acceptance Notification</title>
        <style>
            body {
                font-family: Arial, sans-serif;
                background-color: #f4f4f4;
                margin: 0;
                padding: 0;
            }
            .email-container {
                max-width: 600px;
                margin: 0 auto;
                background-color: #ffffff;
                padding: 20px;
                border-radius: 8px;
                box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
            }
            .header {
                text-align: center;
                padding: 10px;
                background-color: #4CAF50;
                color: #ffffff;
                border-radius: 8px 8px 0 0;
            }
            .content {
                padding: 20px;
            }
            .content h1 {
                color: #333333;
            }
            .content p {
                color: #666666;
                line-height: 1.6;
            }
            .button {
                display: inline-block;
                margin-top: 20px;
                padding: 10px 20px;
                background-color: #4CAF50;
                color: #ffffff;
                text-decoration: none;
                border-radius: 5px;
            }
            .footer {
                margin-top: 30px;
                text-align: center;
                font-size: 12px;
                color: #aaaaaa;
            }
        </style>
    </head>
    <body>
        <div class='email-container'>
            <div class='header'>
                <h2>Job Application Update</h2>
            </div>
            <div class='content'>
                <h1>Congratulations, %s!</h1>
                <p>%s</p>
                <p>Thank you for your interest in joining our team!</p>
            </div>
            <div class='footer'>
                <p>&copy; 2024 Our Company, Inc. All rights reserved.</p>
            </div>
        </div>
    </body>
    </html>
    """, emailRequestDTO.getName(), emailRequestDTO.getContent());
        try {
            emailService.sendEmail(emailRequestDTO.getEmail(), emailRequestDTO.getSubject(), htmlMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email");
        }
    }

    @Override
    public List<JobApplicationDTO> findByJobId(Long jobId) {
        List<JobApplication> jobApplications = jobApplicationRepository.findByJobId(jobId);
        return jobApplications.stream().map(app -> JobApplicationDTO.builder()
                .status(app.getStatus().name())
                .jobId(app.getJob().getId())
                .jobTitle(app.getJob().getTitle())
                .fullName(app.getUser().getFullName())
                .cvId(app.getCv().getId())
                .cvUrl(app.getCv().getCvUrl())
                .applyDate(app.getCreatedAt())
                .id(app.getId())
                .email(app.getUser().getEmail())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public Page<JobApplication> findByUserId(Long userId, PageRequest pageRequest) {
        return jobApplicationRepository.findByUserId(userId, pageRequest);
    }

    @Override
    public JobApplication findByJobIdAndUserId(Long jobId, Long userId) {
        return jobApplicationRepository.findByJobIdAndUserId(jobId, userId);
    }

    @Override
    public void deleteByJobIdAndUserId(Long jobId, Long userId) {
        jobApplicationRepository.deleteByJobIdAndUserId(jobId, userId);
    }

    @Override
    public void deleteByJobId(Long jobId) {
        jobApplicationRepository.deleteByJobId(jobId);
    }

    @Override
    public void deleteByUserId(Long userId) {
        jobApplicationRepository.deleteByUserId(userId);
    }

}
