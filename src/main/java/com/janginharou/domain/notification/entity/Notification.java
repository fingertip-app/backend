package com.janginharou.domain.notification.entity;

import com.janginharou.domain.user.entity.User;
import com.janginharou.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    private Long relatedId;
    private String relatedType;

    @Column(nullable = false)
    private Boolean isRead;

    private LocalDateTime readAt;

    @Column(nullable = false)
    private Boolean isSent;

    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    public void markAsSent() {
        this.isSent = true;
    }

    public void markAsUnsent() {
        this.isSent = false;
    }

    public static Notification of(User user, String title, String message, NotificationType type) {
        return Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .readAt(null)
                .isSent(false)
                .build();
    }

    public static Notification of(User user, String title, String message, NotificationType type,
                                   Long relatedId, String relatedType) {
        return Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .relatedId(relatedId)
                .relatedType(relatedType)
                .isRead(false)
                .readAt(null)
                .isSent(false)
                .build();
    }
}
