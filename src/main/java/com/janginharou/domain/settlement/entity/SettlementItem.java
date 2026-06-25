package com.janginharou.domain.settlement.entity;

import com.janginharou.domain.reservation.entity.Reservation;
import com.janginharou.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "settlement_items")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount; // 해당 예약의 매출

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal feeAmount; // 해당 예약의 수수료

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal settlementAmount; // 해당 예약의 정산 금액
}
