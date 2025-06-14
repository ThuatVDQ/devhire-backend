package com.hcmute.devhire.services;

import com.hcmute.devhire.entities.Notification;

import java.util.List;

public interface INotificationService {
    Notification createNotification(String message, String username, String targetUrl) throws Exception;
    void createAndSendNotification(String message, String username, String targetUrl) throws Exception;
    List<Notification> getUserNotifications(String username) throws Exception;
    void markAsRead(Long id) throws Exception;
    void deleteNotification(Long id) throws Exception;
    void markAllAsRead(String username) throws Exception;
    long countUnreadNotifications(String username) throws Exception;
    void sendNotificationToAdmin(String message, String targetUrl) throws Exception;
}
