package com.janginharou.domain.settlement.dto;

import com.janginharou.domain.settlement.entity.SettlementItem;

import java.math.BigDecimal;

public record SettlementItemResponse(
        Long id,
        Long reservationId,
        String experienceTitle,
        BigDecimal amount,
        BigDecimal feeAmount,
        BigDecimal settlementAmount
) {
    public static SettlementItemResponse from(SettlementItem item) {
        return new SettlementItemResponse(
                item.getId(),
                item.getReservation().getId(),
                item.getReservation().getExperience().getTitle(),
                item.getAmount(),
                item.getFeeAmount(),
                item.getSettlementAmount()
        );
    }
}
