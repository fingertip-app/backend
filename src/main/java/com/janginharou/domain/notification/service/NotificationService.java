package com.janginharou.domain.notification.service;

import com.janginharou.domain.notification.entity.Notification;
import com.janginharou.domain.notification.entity.NotificationType;
import com.janginharou.domain.notification.repository.NotificationRepository;
import com.janginharou.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public Notification getNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }

    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public Notification markAsRead(Long notificationId) {
        Notification notification = getNotificationById(notificationId);
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        getUnreadNotificationsByUserId(userId).forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
        });
    }

    @Async("taskExecutor")
    @Transactional
    public void sendNotificationAsync(Notification notification) {
        // TODO: 푸시 알림 서버 호출 (Firebase Cloud Messaging, OneSignal 등)
        notification.setIsSent(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public Notification createAndSendNotification(Notification notification) {
        notification.setIsRead(false);
        notification.setIsSent(false);
        Notification savedNotification = notificationRepository.save(notification);
        sendNotificationAsync(savedNotification);
        return savedNotification;
    }

    @Transactional(readOnly = true)
    public List<Notification> getPendingNotifications(NotificationType type) {
        return notificationRepository.findByTypeAndIsSentFalse(type);
    }

    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}
