package com.janginharou.domain.reservation.dto;

import com.janginharou.domain.reservation.entity.Booking;
import com.janginharou.domain.reservation.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {

    private Long id;
    private Long userId;
    private Long experienceId;
    private Long scheduleId;
    private Integer participants;
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

    public static ReservationResponse from(Booking booking) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .userId(reservation.getUser().getId())
                .experienceId(reservation.getExperience().getId())
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
                .build();
    }
}
