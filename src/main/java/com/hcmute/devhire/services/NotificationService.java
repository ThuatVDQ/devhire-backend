package com.hcmute.devhire.services;

import com.hcmute.devhire.entities.Notification;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {
    private final NotificationRepository notificationRepository;
    private final IUserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public Notification createNotification(String message, String username) throws Exception {
        User user = userService.findByUserName(username);
        if (user == null) {
            throw new Exception("User not found");
        }
        Notification notification = Notification.builder()
                .message(message)
                .isRead(false)
                .sendAt(Instant.ofEpochSecond(Instant.now().getEpochSecond()))
                .user(user)
                .build();
        return notificationRepository.save(notification);
    }

    @Override
    public Notification createAndSendNotification(String message, String username) throws Exception {
        Notification notification = createNotification(message, username);

        messagingTemplate.convertAndSend("/topic/notifications/" + username, notification);

        return notification;
    }

    @Override
    public List<Notification> getUserNotifications(String username) throws Exception {
        User user = userService.findByUserName(username);
        return notificationRepository.findByUserId(user.getId());
    }

    @Override
    public void markAsRead(Long id) throws Exception {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void deleteNotification(Long id) throws Exception {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notificationRepository.delete(notification);
    }

    @Override
    public void markAllAsRead(String username) throws Exception {
        User user = userService.findByUserName(username);
        List<Notification> notifications = notificationRepository.findByUserId(user.getId());
        notifications.forEach(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
    }

    @Override
    public long countUnreadNotifications(String username) throws Exception {
        User user = userService.findByUserName(username);
        List<Notification> notifications = notificationRepository.findByUserId(user.getId());
        return notifications.stream().filter(notification -> !notification.getIsRead()).count();
    }
}
