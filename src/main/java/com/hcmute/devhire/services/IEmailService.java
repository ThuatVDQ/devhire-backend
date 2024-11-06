package com.hcmute.devhire.services;

import jakarta.mail.MessagingException;

public interface IEmailService {
    void sendVerificationEmail(String to, String subject, String text) throws MessagingException;
}
