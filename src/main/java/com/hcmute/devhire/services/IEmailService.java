package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.JobDTO;
import jakarta.mail.MessagingException;

import java.util.List;

public interface IEmailService {
    void sendEmail(String to, String subject, String text) throws MessagingException;
    void sendEmail(String to, String subject, List<JobDTO> jobs) throws MessagingException;
}

