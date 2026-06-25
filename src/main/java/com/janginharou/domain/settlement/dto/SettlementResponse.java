package com.janginharou.domain.settlement.dto;

import com.janginharou.domain.settlement.entity.Settlement;
import com.janginharou.domain.settlement.entity.SettlementStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SettlementResponse(
        Long id,
        Long artisanId,
        String artisanName,
        LocalDate settlementStartDate,
        LocalDate settlementEndDate,
        BigDecimal totalRevenue,
        BigDecimal platformFeeRate,
        BigDecimal platformFee,
        BigDecimal settlementAmount,
        SettlementStatus status,
        LocalDateTime completedAt,
        LocalDateTime createdAt
) {
    public static SettlementResponse from(Settlement settlement) {
        return new SettlementResponse(
                settlement.getId(),
                settlement.getArtisan().getId(),
                settlement.getArtisan().getName(),
                settlement.getSettlementStartDate(),
                settlement.getSettlementEndDate(),
                settlement.getTotalRevenue(),
                settlement.getPlatformFeeRate(),
                settlement.getPlatformFee(),
                settlement.getSettlementAmount(),
                settlement.getStatus(),
                settlement.getCompletedAt(),
                settlement.getCreatedAt()
        );
    }
}
