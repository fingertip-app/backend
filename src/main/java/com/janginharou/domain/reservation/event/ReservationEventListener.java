package com.janginharou.domain.reservation.event;

import com.janginharou.domain.notification.entity.Notification;
import com.janginharou.domain.notification.service.NotificationService;
import com.janginharou.domain.reservation.entity.ReservationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 예약 상태 변경 이벤트 리스너
 * 비동기로 알림을 생성하고 발송합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventListener {

    private final NotificationService notificationService;

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReservationStatusChanged(ReservationStatusChangedEvent event) {
        log.info("Handling reservation status change: reservationId={}, oldStatus={}, newStatus={}",
                event.getReservationId(), event.getOldStatus(), event.getNewStatus());

        try {
            String title = generateNotificationTitle(event.getNewStatus(), event.getExperienceTitle());
            String body = generateNotificationBody(event);

            Notification notification = Notification.builder()
                    .user(event.getUser())
                    .title(title)
                    .body(body)
                    .build();

            notificationService.createNotification(notification);

            log.info("Notification created successfully for user: {}", event.getUser().getId());
        } catch (Exception e) {
            log.error("Failed to create notification for reservation: {}", event.getReservationId(), e);
            // 알림 실패가 예약 처리 자체를 방해하지 않도록 예외를 삼킴
        }
    }

    private String generateNotificationTitle(ReservationStatus status, String experienceTitle) {
        return switch (status) {
            case APPROVED -> "예약이 승인되었습니다";
            case REJECTED -> "예약이 거절되었습니다";
            case PAID -> "결제가 완료되었습니다";
            case CONFIRMED -> "예약이 확정되었습니다";
            case CANCELLED -> "예약이 취소되었습니다";
            case COMPLETED -> "체험이 완료되었습니다";
            default -> "예약 상태가 변경되었습니다";
        };
    }

    private String generateNotificationBody(ReservationStatusChangedEvent event) {
        String experienceTitle = event.getExperienceTitle();
        ReservationStatus newStatus = event.getNewStatus();

        return switch (newStatus) {
            case APPROVED -> String.format("'%s' 체험 예약이 장인에게 승인되었습니다. 결제를 진행해주세요.", experienceTitle);
            case REJECTED -> {
                String reason = event.getReason() != null ? "\n사유: " + event.getReason() : "";
                yield String.format("'%s' 체험 예약이 거절되었습니다.%s", experienceTitle, reason);
            }
            case PAID -> String.format("'%s' 체험 결제가 완료되었습니다.", experienceTitle);
            case CONFIRMED -> String.format("'%s' 체험 예약이 최종 확정되었습니다. QR 확인서를 확인하세요.", experienceTitle);
            case CANCELLED -> {
                String reason = event.getReason() != null ? "\n사유: " + event.getReason() : "";
                yield String.format("'%s' 체험 예약이 취소되었습니다.%s", experienceTitle, reason);
            }
            case COMPLETED -> String.format("'%s' 체험이 완료되었습니다. 후기를 남겨주세요!", experienceTitle);
            default -> String.format("'%s' 체험 예약 상태가 변경되었습니다.", experienceTitle);
        };
    }
}
