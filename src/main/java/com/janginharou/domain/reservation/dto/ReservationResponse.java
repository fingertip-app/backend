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
    private BookingStatus status;
    private String requestMessage;
    private String qrCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReservationResponse from(Booking booking) {
        return ReservationResponse.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .experienceId(booking.getExperience().getId())
                .scheduleId(booking.getSchedule().getId())
                .participants(booking.getParticipants())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .requestMessage(booking.getRequestMessage())
                .qrCode(booking.getQrCode())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
