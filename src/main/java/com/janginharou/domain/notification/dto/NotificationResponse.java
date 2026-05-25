package com.janginharou.domain.notification.dto;

import com.janginharou.domain.notification.entity.Notification;
import com.janginharou.domain.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private Long userId;
    private String title;
    private String message;
    private NotificationType type;
    private Long relatedId;
    private String relatedType;
    private Boolean isRead;
    private LocalDateTime readAt;
    private Boolean isSent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .relatedId(notification.getRelatedId())
                .relatedType(notification.getRelatedType())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .isSent(notification.getIsSent())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}
