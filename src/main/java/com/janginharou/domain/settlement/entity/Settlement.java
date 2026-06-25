package com.janginharou.domain.settlement.entity;

import com.janginharou.domain.artisan.entity.Artisan;
import com.janginharou.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlements")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Settlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artisan_id", nullable = false)
    private Artisan artisan;

    @Column(nullable = false)
    private LocalDate settlementStartDate; // 정산 기간 시작

    @Column(nullable = false)
    private LocalDate settlementEndDate; // 정산 기간 종료

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalRevenue; // 총 매출 (수수료 제외 전)

    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal platformFeeRate = BigDecimal.valueOf(0.10); // 플랫폼 수수료율 (기본 10%)

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal platformFee; // 플랫폼 수수료

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal settlementAmount; // 정산 금액 (실제 송금 금액)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SettlementStatus status = SettlementStatus.PENDING;

    private LocalDateTime completedAt; // 정산 완료 시각

    @Column(columnDefinition = "TEXT")
    private String bankAccount; // 정산 계좌 (Mock용)

    @Column(length = 200)
    private String transferReference; // 송금 참조번호 (Mock용)

    public void process() {
        this.status = SettlementStatus.PROCESSING;
    }

    public void complete(String transferReference) {
        this.status = SettlementStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.transferReference = transferReference;
    }

    public void fail() {
        this.status = SettlementStatus.FAILED;
    }
}
