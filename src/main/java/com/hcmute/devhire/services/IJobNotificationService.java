package com.hcmute.devhire.services;

public interface IJobNotificationService {
    String subscribe(String email);
    void sendJobNotifications();
}
