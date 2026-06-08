package com.janginharou.domain.reservation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationRequest {

    @NotNull
    private Long experienceId;

    @NotNull
    private Long scheduleId;

    @NotNull
    @Positive
    private Integer numberOfParticipants;

    @NotNull
    private LocalDateTime reservedDateTime;

    private String requestMessage;
}
