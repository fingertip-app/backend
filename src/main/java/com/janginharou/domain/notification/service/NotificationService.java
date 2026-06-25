package com.janginharou.domain.notification.service;

import com.janginharou.domain.notification.entity.Notification;
import com.janginharou.domain.notification.repository.NotificationRepository;
import com.janginharou.global.exception.ResourceNotFoundException;
import com.janginharou.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Notification markAsRead(Long notificationId, Long userId) {
        Notification notification = getNotificationById(notificationId);
        validateUserOwnsNotification(notification, userId);
        notification.markAsRead();
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        getUnreadNotificationsByUserId(userId).forEach(notification -> {
            notification.markAsRead();
            notificationRepository.save(notification);
        });
    }

    @Transactional
    public Notification createNotification(Notification notification) {
        // TODO: 푸시 알림 서버 호출 (Firebase Cloud Messaging, OneSignal 등)
        return notificationRepository.save(notification);
    }

    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = getNotificationById(notificationId);
        validateUserOwnsNotification(notification, userId);
        notificationRepository.deleteById(notificationId);
    }

    private void validateUserOwnsNotification(Notification notification, Long userId) {
        if (!notification.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not own this notification");
        }
    }

    public void validateUserId(Long pathUserId, Long authenticatedUserId) {
        if (!pathUserId.equals(authenticatedUserId)) {
            throw new UnauthorizedException("You cannot access another user's notifications");
        }
    }
}
