package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.*;
import com.hcmute.devhire.entities.*;
import com.hcmute.devhire.repositories.InterviewScheduleRepository;
import com.hcmute.devhire.repositories.JobApplicationRepository;
import com.hcmute.devhire.utils.InterviewResult;
import com.hcmute.devhire.utils.JobApplicationStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class InterviewScheduleService implements IInterviewScheduleService{
    private final InterviewScheduleRepository interviewScheduleRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final INotificationService notificationService;
    private final IEmailService emailService;
    private final ICompanyService companyService;

    @Override
    @Transactional
    public InterviewSchedule createInterviewSchedule(InterviewScheduleDTO dto) throws Exception {
        JobApplication jobApplication = jobApplicationRepository.findById(dto.getJobApplicationId())
                .orElseThrow(() -> new RuntimeException("Job application not found"));

        jobApplication.setScheduled(true);
        jobApplicationRepository.save(jobApplication);

        User interviewer = jobApplication.getUser();
        Job job = jobApplication.getJob();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm, EEEE, dd/MM/yyyy")
                .withLocale(Locale.forLanguageTag("vi-VI"));
        String formattedTime = dto.getInterviewTime().format(formatter);

        String emailSubject = String.format("[DevHire] Interview schedule for position %s", job.getTitle());

        String htmlMessage = String.format("""
<!DOCTYPE html>
<html lang='en'>
<head>
    <meta charset='UTF-8'>
    <meta name='viewport' content='width=device-width, initial-scale=1.0'>
    <title>Interview Invitation</title>
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
        .content p, .content li {
            color: #666666;
            line-height: 1.6;
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
            <h2>Interview Invitation</h2>
        </div>
        <div class='content'>
            <h1>Hello %s,</h1>
            <p>You have been invited to an interview for the following position:</p>
            <ul>
                <li><strong>Interviewer:</strong> %s</li>
                <li><strong>Position:</strong> %s</li>
                <li><strong>Time:</strong> %s</li>
                <li><strong>Duration:</strong> %d minutes</li>
                <li><strong>Location:</strong> %s</li>
            </ul>
            <p><strong>Note:</strong><br>%s</p>
            <p>Please confirm your participation and check details on the DevHire platform.</p>
        </div>
        <div class='footer'>
            <p>&copy; 2024 DevHire. All rights reserved.</p>
        </div>
    </div>
</body>
</html>
""",
                interviewer.getFullName(),
                jobApplication.getUser().getFullName(),
                job.getTitle(),
                formattedTime,
                dto.getDurationMinutes(),
                formatLocation(dto.getLocation()),
                dto.getNote() != null ? dto.getNote() : "Empty"
        );

        // Gửi thông báo và email
        notificationService.createAndSendNotification(
                String.format("Interview schedule %s - %s", job.getTitle(), formattedTime),
                interviewer.getUsername(), "/settings"
        );

        emailService.sendEmail(
                interviewer.getUsername(),
                emailSubject,
                htmlMessage
        );

        return interviewScheduleRepository.save(
                InterviewSchedule.builder()
                        .jobApplication(jobApplication)
                        .interviewTime(dto.getInterviewTime())
                        .durationMinutes(dto.getDurationMinutes())
                        .location(dto.getLocation())
                        .note(dto.getNote())
                        .result(InterviewResult.WAITING)
                        .build()
        );
    }

    private String formatLocation(String location) {
        if (location.startsWith("http")) {
            return "Online interview: " + location;
        }
        return "At direction: " + location;
    }

    @Transactional
    public List<InterviewSchedule> createBulkSchedules(InterviewScheduleBulkDTO dto) throws Exception {
        List<InterviewSchedule> schedules = new ArrayList<>();

        for (Long jobAppId : dto.getJobApplicationIds()) {
            JobApplication jobApp = jobApplicationRepository.findById(jobAppId)
                    .orElseThrow(() -> new EntityNotFoundException("Job application not found: " + jobAppId));

            jobApp.setScheduled(true);
            jobApplicationRepository.save(jobApp);

            InterviewSchedule schedule = InterviewSchedule.builder()
                    .jobApplication(jobApp)
                    .interviewTime(dto.getInterviewTime())
                    .durationMinutes(dto.getDurationMinutes())
                    .location(dto.getLocation())
                    .note(dto.getNote())
                    .result(InterviewResult.WAITING)
                    .build();

            schedules.add(schedule);

            // Gửi thông báo
            sendNotification(jobApp, dto);
        }

        return interviewScheduleRepository.saveAll(schedules);
    }

    @Override
    public Page<InterviewScheduleDTO> getInterviewResults(String username, InterviewResult status, PageRequest pageRequest) {
        Company company = companyService.findByUser(username);
        if (company == null) {
            throw new RuntimeException("Company not found");
        }

        return interviewScheduleRepository.findAllByCompanyIdAndInterviewStatus(
                company.getId(),
                status,
                pageRequest
        ).map(this::convertToDTO);
    }

    @Override
    public InterviewSchedule updateInterviewResult(Long id, InterviewResultDTO dto) throws Exception {
        InterviewSchedule schedule = interviewScheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Interview schedule not found"));
        schedule.setResult(dto.getResult());
        schedule.setRecruiterNote(dto.getRecruiterNote());
        sendNotification(schedule, dto.getRecruiterNote());
        return interviewScheduleRepository.save(schedule);
    }

    @Override
    public void sendEmail(Long id, EmailRequestDTO emailRequestDTO) throws Exception {
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
                <h2>Your interview result</h2>
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
        InterviewSchedule schedule = interviewScheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Interview schedule not found"));

        if (schedule.isEmailSent()) {
            throw new Exception("Email already sent for this schedule");
        }
        // Gửi thông báo và email
        emailService.sendEmail(emailRequestDTO.getEmail(), emailRequestDTO.getSubject(), htmlMessage);
        schedule.setEmailSent(true);
        interviewScheduleRepository.save(schedule);
    }

    private void sendNotification(InterviewSchedule schedule, String note) throws Exception {
        User candidate = schedule.getJobApplication().getUser();
        Job job = schedule.getJobApplication().getJob();

        String result = schedule.getResult().name();
        String title = String.format("Interview result for %s", job.getTitle());
        String message = String.format(
                "Interview result for %s: %s", job.getTitle(),
                result
        );

        // Gửi notification trong hệ thống
        notificationService.createAndSendNotification(message, candidate.getUsername(), "/settings");
    }

    private void sendNotification(JobApplication jobApp, InterviewScheduleBulkDTO dto) throws Exception {
        User interviewer = jobApp.getUser();
        Job job = jobApp.getJob();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy");
        String formattedTime = dto.getInterviewTime().format(formatter);

        String emailContent = String.format(
                "Dear %s,\n\n" +
                        "You have an interview schedule arranged as follows:\n\n" +
                        "**Interviewer:** %s\n" +
                        "**Position:** %s\n" +
                        "**Time:** %s\n" +
                        "**Duration:** %d minutes\n" +
                        "**Location:** %s\n\n" +
                        "**Note:**\n%s\n\n" +
                        "Please confirm your participation and double check the information on the DevHire system.\n\n" +
                        "Best regards,\n" +
                        "DevHire Team",
                interviewer.getFullName(),
                jobApp.getUser().getFullName(),
                job.getTitle(),
                formattedTime,
                dto.getDurationMinutes(),
                formatLocation(dto.getLocation()),
                dto.getNote() != null ? dto.getNote() : "Empty"
        );

        notificationService.createAndSendNotification(String.format("Interview schedule %s - %s", job.getTitle(), formattedTime),
                interviewer.getUsername(), "/settings");
        emailService.sendEmail(interviewer.getUsername(), "[DevHire] New interview schedule", emailContent);
    }

    @Override
    @Transactional
    public InterviewSchedule updateInterviewSchedule(Long id, InterviewScheduleUpdateDTO dto) throws Exception {
        InterviewSchedule existingSchedule = interviewScheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Interview schedule not found"));

        // Kiểm tra trùng lịch phỏng vấn
        checkInterviewConflict(
                existingSchedule.getJobApplication().getUser().getId(),
                dto.getInterviewTime(),
                dto.getDurationMinutes(),
                id
        );

        // Cập nhật thông tin
        existingSchedule.setInterviewTime(dto.getInterviewTime());
        existingSchedule.setDurationMinutes(dto.getDurationMinutes());
        existingSchedule.setLocation(dto.getLocation());
        existingSchedule.setNote(dto.getNote());

        // Gửi thông báo
        sendUpdateNotification(existingSchedule);

        return interviewScheduleRepository.save(existingSchedule);
    }

    @Override
    public Page<InterviewScheduleDTO> getAllInterviewSchedules(String username, PageRequest pageRequest) throws Exception {

        Company company = companyService.findByUser(username);
        if (company == null) {
            throw new Exception("Company not found");
        }

        return interviewScheduleRepository.findAllByCompanyId(company.getId(), pageRequest)
                .map(this::convertToDTO);
    }

    @Override
    public Page<InterviewScheduleDTO> getUserInterviewSchedules(String username, PageRequest pageRequest) throws Exception {
        return interviewScheduleRepository.findAllByUserEmail(username, pageRequest)
                .map(this::convertToDTO);
    }

    @Override
    public Page<InterviewScheduleDTO> getByStatus(String username, String status, PageRequest pageRequest) throws Exception {
        Company company = companyService.findByUser(username);
        if (company == null) {
            throw new Exception("Company not found");
        }

        return interviewScheduleRepository.findAllByCompanyIdAndStatus(company.getId(), JobApplicationStatus.valueOf(status), pageRequest)
                .map(this::convertToDTO);
    }

    private InterviewScheduleDTO convertToDTO(InterviewSchedule schedule) {
        JobApplication jobApp = schedule.getJobApplication();
        User candidate = jobApp.getUser();
        Job job = jobApp.getJob();

        return InterviewScheduleDTO.builder()
                .id(schedule.getId())
                .interviewTime(schedule.getInterviewTime())
                .durationMinutes(schedule.getDurationMinutes())
                .location(schedule.getLocation())
                .note(schedule.getNote())
                .candidateName(candidate.getFullName())
                .candidateEmail(candidate.getEmail())
                .jobTitle(job.getTitle())
                .jobId(job.getId())
                .applicationStatus(jobApp.getStatus())
                .result(schedule.getResult())
                .recruiterNote(schedule.getRecruiterNote())
                .emailSent(schedule.isEmailSent())
                .build();
    }

    private void checkInterviewConflict(Long userId, LocalDateTime newTime, int duration, Long excludeId) throws Exception {
        LocalDateTime endTime = newTime.plusMinutes(duration);
        int hasConflict = interviewScheduleRepository.existsByUserAndTimeRange(
                userId,
                newTime,
                endTime,
                excludeId
        );

        if (hasConflict > 0) {
            throw new Exception("User has conflicting interview schedule");
        }
    }

    private void sendUpdateNotification(InterviewSchedule schedule) throws Exception {
        JobApplication jobApp = schedule.getJobApplication();
        User interviewer = jobApp.getUser();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy");

        String content = String.format(
                "The interview schedule for the position %s has been updated:\n" +
                        "- New time: %s\n" +
                        "- Location: %s\n" +
                        "- Note: %s",
                jobApp.getJob().getTitle(),
                schedule.getInterviewTime().format(formatter),
                schedule.getLocation(),
                StringUtils.defaultString(schedule.getNote(), "No note")
        );

        notificationService.createAndSendNotification(
                "Interview schedule updated: " + content,
                interviewer.getUsername(), "/settings"
        );

        emailService.sendEmail(
                interviewer.getUsername(),
                "[DevHire] Interview schedule updated",
                generateUpdateEmailContent(schedule)
        );
    }

    private String generateUpdateEmailContent(InterviewSchedule schedule) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy");
        String formattedTime = schedule.getInterviewTime().format(formatter);

        return String.format("""
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <style>
            body { font-family: Arial, sans-serif; line-height: 1.6; }
            .header { color: #2c3e50; font-size: 24px; margin-bottom: 20px; }
            .details { margin: 15px 0; }
            .footer { margin-top: 30px; color: #7f8c8d; }
        </style>
    </head>
    <body>
        <div class="header">📅 Interview Schedule Updated</div>
        
        <div class="details">
            <p><strong>Candidate:</strong> %s</p>
            <p><strong>Position:</strong> %s</p>
            <p><strong>Time:</strong> %s</p>
            <p><strong>Location:</strong> %s</p>
            <p><strong>Note:</strong> %s</p>
        </div>

        <div class="footer">
            <p>Best regards,<br>DevHire Team</p>
            <p><em>This is an automated email, please do not reply.</em></p>
        </div>
    </body>
    </html>
    """,
                schedule.getJobApplication().getUser().getFullName(),
                schedule.getJobApplication().getJob().getTitle(),
                formattedTime,
                formatLocation(schedule.getLocation()),
                schedule.getNote() != null ? schedule.getNote() : "No note"
        );
    }
}
