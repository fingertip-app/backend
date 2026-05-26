package com.janginharou.domain.reservation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private Integer participants;

    private String requestMessage;
}
