package com.janginharou.domain.qr.dto;

import com.janginharou.domain.reservation.entity.Reservation;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QrVerifyResponse {
    private boolean valid;
    private Long reservationId;
    private String userName;
    private String experienceTitle;
    private String scheduledAt;
    private Integer participants;
    private String status;

    public static QrVerifyResponse from(Reservation reservation) {
        return QrVerifyResponse.builder()
                .valid(true)
                .reservationId(reservation.getId())
                .userName(reservation.getUser().getNickname())
                .experienceTitle(reservation.getExperience().getTitle())
                .scheduledAt(reservation.getSchedule().getScheduledAt().toString())
                .participants(reservation.getNumberOfParticipants())
                .status(reservation.getStatus().name())
                .build();
    }
}
