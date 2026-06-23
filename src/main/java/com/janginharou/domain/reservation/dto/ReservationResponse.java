package com.janginharou.domain.reservation.dto;

import com.janginharou.domain.experience.dto.ExperienceWithReviewsDto;
import com.janginharou.domain.reservation.entity.Reservation;
import com.janginharou.domain.reservation.entity.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 예약 응답 DTO
 * - include=experience 쿼리 파라미터로 체험 정보 포함/제외 가능
 * - 기본적으로 체험 정보는 포함되지 않음 (성능 최적화)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReservationResponse {

    private Long id;
    private Long userId;
    private Long experienceId;
    private Long scheduleId;
    private Integer numberOfParticipants;
    private BigDecimal totalPrice;
    private ReservationStatus status;
    private LocalDateTime reservedDateTime;
    private String rejectionReason;
    private String cancellationReason;
    private String requestMessage;
    private String paymentKey;
    private String paymentOrderId;
    private Boolean isNotificationSent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 선택적 필드: include=experience 쿼리 파라미터로 포함됨
    private ExperienceWithReviewsDto experience;

    /**
     * 기본 from - 체험 정보 없음
     */
    public static ReservationResponse from(Reservation reservation) {
        return from(reservation, null);
    }

    /**
     * include=experience 옵션으로 체험 정보 포함
     */
    public static ReservationResponse from(Reservation reservation, ExperienceWithReviewsDto experienceDto) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .userId(reservation.getUser().getId())
                .experienceId(reservation.getExperience().getId())
                .scheduleId(reservation.getSchedule().getId())
                .numberOfParticipants(reservation.getNumberOfParticipants())
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus())
                .reservedDateTime(reservation.getReservedDateTime())
                .rejectionReason(reservation.getRejectionReason())
                .cancellationReason(reservation.getCancellationReason())
                .requestMessage(reservation.getRequestMessage())
                .paymentKey(reservation.getPaymentKey())
                .paymentOrderId(reservation.getPaymentOrderId())
                .isNotificationSent(reservation.getIsNotificationSent())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .experience(experienceDto)
                .build();
    }
}
