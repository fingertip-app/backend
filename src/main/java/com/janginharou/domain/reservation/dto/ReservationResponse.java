package com.janginharou.domain.reservation.dto;

import com.janginharou.domain.reservation.entity.Reservation;
import com.janginharou.domain.reservation.entity.ReservationStatus;
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
    private Integer numberOfParticipants;
    private BigDecimal totalPrice;
    private ReservationStatus status;
    private LocalDateTime reservedDateTime;
    private String rejectionReason;
    private String cancellationReason;
    private Boolean isNotificationSent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReservationResponse from(Reservation reservation) {
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
                .isNotificationSent(reservation.getIsNotificationSent())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }
}
