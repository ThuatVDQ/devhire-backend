package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.EmailRequestDTO;
import com.hcmute.devhire.DTOs.JobApplicationDTO;
import com.hcmute.devhire.DTOs.JobApplicationWithScoreDTO;
import com.hcmute.devhire.entities.*;
import com.hcmute.devhire.exceptions.DataNotFoundException;
import com.hcmute.devhire.repositories.JobApplicationRepository;
import com.hcmute.devhire.responses.CountPerJobResponse;
import com.hcmute.devhire.responses.CvScoreResponse;
import com.hcmute.devhire.responses.MonthlyCountResponse;
import com.hcmute.devhire.utils.JobApplicationStatus;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobApplicationService implements IJobApplicationService{
    private final JobApplicationRepository jobApplicationRepository;
    private final IEmailService emailService;
    private final INotificationService notificationService;
    private final ICompanyService companyService;
    private final ICvScoringService cvScoringService;

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
                .jobTitle(jobApplication.getJob().getTitle())
                .fullName(jobApplication.getUser().getFullName())
                .email(jobApplication.getUser().getEmail())
                .phone(jobApplication.getUser().getPhone())
                .cvUrl(jobApplication.getCv().getCvUrl())
                .applyDate(jobApplication.getCreatedAt())
                .id(jobApplication.getId())
                .isScheduled(jobApplication.isScheduled())
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
        jobApplication.setStatus(JobApplicationStatus.IN_PROGRESS);
        jobApplicationRepository.save(jobApplication);
    }

    @Override
    public void seenJobApplication(Long jobApplicationId) {
        try {
            JobApplication jobApplication = jobApplicationRepository.findById(jobApplicationId).get();
            jobApplication.setStatus(JobApplicationStatus.SEEN);
            notificationService.createAndSendNotification("The recruiter, " + jobApplication.getJob().getCompany().getName() + " just viewed your CV.", jobApplication.getUser().getUsername());
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
            notificationService.createAndSendNotification("The recruiter, " + jobApplication.getJob().getCompany().getName() + " just updated your cv status is not suitable", jobApplication.getUser().getUsername());
            notificationService.sendNotificationToAdmin(jobApplication.getJob().getCompany().getName() + " has rejected a job application");
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
            notificationService.createAndSendNotification("The recruiter, " + jobApplication.getJob().getCompany().getName() + " just updated your cv status suitable.", jobApplication.getUser().getUsername());
            notificationService.sendNotificationToAdmin(jobApplication.getJob().getCompany().getName() + " has accept a job application");
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
    public int getTotalJobApplication(String username) {
        Company company = companyService.findByUser(username);
        if (company != null) {
            return jobApplicationRepository.countByCompanyId(company.getId());
        } else {
            throw new RuntimeException("Company not found");
        }
    }

    @Override
    public List<CountPerJobResponse> countJobApplicationPerJob(String username) {
        Company company = companyService.findByUser(username);
        if (company != null) {
            return jobApplicationRepository.countApplicationsPerJobByCompany(company.getId());
        } else {
            throw new RuntimeException("Company not found");
        }
    }

    @Override
    public List<MonthlyCountResponse> countJobApplicationByMonth(int year, String username) {
        Company company = companyService.findByUser(username);
        if (company != null) {
            return jobApplicationRepository.countApplicationsByMonthForCompany(year, company.getId());
        } else {
            throw new RuntimeException("Company not found");
        }
    }

    @Override
    public List<CountPerJobResponse> countJobApplicationPerJob() {
        return jobApplicationRepository.countApplicationsPerJob();
    }

    @Override
    public List<MonthlyCountResponse> countJobApplicationByMonth(int year) {
        return jobApplicationRepository.countApplicationsByMonthForCompany(year);
    }

    @Override
    public List<JobApplicationWithScoreDTO> getScoredApplicationsByJob(Long jobId, JobApplicationStatus status) throws IOException {
        List<JobApplication> applications = (status != null)
                ? jobApplicationRepository.findByJobIdAndStatus(jobId, status)
                : jobApplicationRepository.findByJobId(jobId);

        List<JobApplicationWithScoreDTO> result = new ArrayList<>();

        for (JobApplication app : applications) {
            if (app.getCv() == null || app.getCv().getCvUrl() == null) continue;

            Path path = Paths.get("uploads", app.getCv().getCvUrl());
            if (!Files.exists(path)) continue;

            MultipartFile multipartFile = new MockMultipartFile(
                    app.getCv().getCvUrl(),
                    Files.readAllBytes(path)
            );

            CvScoreResponse scoreResponse = cvScoringService.calculateCvScore(multipartFile, jobId);

            result.add(JobApplicationWithScoreDTO.builder()
                    .applicationId(app.getId())
                    .applicantName(app.getUser().getFullName())
                    .applicantEmail(app.getUser().getEmail())
                    .score(scoreResponse.getTotalScore() * 100)
                    .scoreDetails(scoreResponse.getScoreDetails())
                    .build());
        }

        return result.stream()
                .sorted(Comparator.comparingDouble(JobApplicationWithScoreDTO::getScore).reversed())
                .toList();
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
                .isScheduled(app.isScheduled())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public List<JobApplicationDTO> findByJobIdAndStatus(Long jobId, String status) {
        List<JobApplication> jobApplications = jobApplicationRepository.findByJobIdAndStatus(jobId, JobApplicationStatus.valueOf(status));
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
                .isScheduled(app.isScheduled())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public Page<JobApplication> findByUserId(Long userId, PageRequest pageRequest) {
        return jobApplicationRepository.findByUserId(userId, pageRequest);
    }

    @Override
    public Page<JobApplicationDTO> getByUserId(Long userId, PageRequest pageRequest) {
        Page<JobApplication> jobApplications = jobApplicationRepository.findByUserId(userId, pageRequest);
        if (jobApplications != null) {
            return jobApplications.map(app -> JobApplicationDTO.builder()
                    .status(app.getStatus().name())
                    .jobId(app.getJob().getId())
                    .jobTitle(app.getJob().getTitle())
                    .cvId(app.getCv().getId())
                    .cvUrl(app.getCv().getCvUrl())
                    .applyDate(app.getCreatedAt())
                    .id(app.getId())
                    .isScheduled(app.isScheduled())
                    .build()
            );
        }
        return null;
    }

    @Override
    public Page<JobApplicationDTO> getAcceptedApplicationsByUserId(Long userId, PageRequest pageRequest) {
        return jobApplicationRepository.findByUserIdAndStatus(userId, JobApplicationStatus.ACCEPTED, pageRequest)
                .map(app -> JobApplicationDTO.builder()
                        .status(app.getStatus().name())
                        .jobId(app.getJob().getId())
                        .jobTitle(app.getJob().getTitle())
                        .cvId(app.getCv().getId())
                        .cvUrl(app.getCv().getCvUrl())
                        .applyDate(app.getCreatedAt())
                        .isScheduled(app.isScheduled())
                        .id(app.getId())
                        .build()
                );
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
