package com.hcmute.devhire.services;

public interface IJobNotificationService {
    String subscribe(String email) throws Exception;
    String unsubscribe(String email) throws Exception;
    boolean checkSubscribed(String email) throws Exception;
    void sendJobNotifications();
}
