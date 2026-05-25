package com.janginharou.domain.notification.repository;

import com.janginharou.domain.notification.entity.Notification;
import com.janginharou.domain.notification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserId(Long userId);

    List<Notification> findByUserIdAndIsReadFalse(Long userId);

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByTypeAndIsSentFalse(NotificationType type);

    long countByUserIdAndIsReadFalse(Long userId);
}
