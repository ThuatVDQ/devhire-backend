package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.InterviewScheduleBulkDTO;
import com.hcmute.devhire.DTOs.InterviewScheduleDTO;
import com.hcmute.devhire.DTOs.InterviewScheduleUpdateDTO;
import com.hcmute.devhire.entities.*;
import com.hcmute.devhire.repositories.InterviewScheduleRepository;
import com.hcmute.devhire.repositories.JobApplicationRepository;
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

        User interviewer = jobApplication.getUser();
        Job job = jobApplication.getJob();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm, EEEE, dd/MM/yyyy")
                .withLocale(Locale.forLanguageTag("vi-VI"));
        String formattedTime = dto.getInterviewTime().format(formatter);

        String emailSubject = String.format("[DevHire] Interview schedule for position %s", job.getTitle());

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
                interviewer.getUsername()
        );

        emailService.sendEmail(
                interviewer.getUsername(),
                emailSubject,
                emailContent
        );

        return interviewScheduleRepository.save(
                InterviewSchedule.builder()
                        .jobApplication(jobApplication)
                        .interviewTime(dto.getInterviewTime())
                        .durationMinutes(dto.getDurationMinutes())
                        .location(dto.getLocation())
                        .note(dto.getNote())
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

            InterviewSchedule schedule = InterviewSchedule.builder()
                    .jobApplication(jobApp)
                    .interviewTime(dto.getInterviewTime())
                    .durationMinutes(dto.getDurationMinutes())
                    .location(dto.getLocation())
                    .note(dto.getNote())
                    .build();

            schedules.add(schedule);

            // Gửi thông báo
            sendNotification(jobApp, dto);
        }

        return interviewScheduleRepository.saveAll(schedules);
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
                interviewer.getUsername());
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
                .build();
    }

    private void checkInterviewConflict(Long userId, LocalDateTime newTime, int duration, Long excludeId) throws Exception {
        LocalDateTime endTime = newTime.plusMinutes(duration);
        boolean hasConflict = interviewScheduleRepository.existsByUserAndTimeRange(
                userId,
                newTime,
                endTime,
                excludeId
        );

        if (hasConflict) {
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
                interviewer.getUsername()
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
