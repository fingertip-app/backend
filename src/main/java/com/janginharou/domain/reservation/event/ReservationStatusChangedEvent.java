package com.janginharou.domain.reservation.event;

import com.janginharou.domain.reservation.entity.ReservationStatus;
import com.janginharou.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 예약 상태 변경 이벤트
 * 예약 승인, 거절, 결제, 확정, 취소 시 발행되어 알림 생성을 트리거합니다.
 */
@Getter
@Builder
@AllArgsConstructor
public class ReservationStatusChangedEvent {

    private final Long reservationId;
    private final User user; // 예약한 사용자
    private final User artisanUser; // 장인의 User 객체
    private final Long experienceId;
    private final String experienceTitle;
    private final ReservationStatus oldStatus;
    private final ReservationStatus newStatus;
    private final String reason; // 거절/취소 사유
    private final LocalDateTime timestamp;

    public static ReservationStatusChangedEvent of(
            Long reservationId,
            User user,
            User artisanUser,
            Long experienceId,
            String experienceTitle,
            ReservationStatus oldStatus,
            ReservationStatus newStatus,
            String reason
    ) {
        return ReservationStatusChangedEvent.builder()
                .reservationId(reservationId)
                .user(user)
                .artisanUser(artisanUser)
                .experienceId(experienceId)
                .experienceTitle(experienceTitle)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .reason(reason)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
