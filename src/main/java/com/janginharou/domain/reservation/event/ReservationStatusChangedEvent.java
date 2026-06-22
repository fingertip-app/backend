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
    private final User user; // userId 대신 User 객체로 변경하여 리스너에서 DB 조회 불필요
    private final Long experienceId;
    private final String experienceTitle;
    private final ReservationStatus oldStatus;
    private final ReservationStatus newStatus;
    private final String reason; // 거절/취소 사유
    private final LocalDateTime timestamp;

    public static ReservationStatusChangedEvent of(
            Long reservationId,
            User user,
            Long experienceId,
            String experienceTitle,
            ReservationStatus oldStatus,
            ReservationStatus newStatus,
            String reason
    ) {
        return ReservationStatusChangedEvent.builder()
                .reservationId(reservationId)
                .user(user)
                .experienceId(experienceId)
                .experienceTitle(experienceTitle)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .reason(reason)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
