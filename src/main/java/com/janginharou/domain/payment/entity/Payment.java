package com.janginharou.domain.payment.entity;

import com.janginharou.domain.reservation.entity.Reservation;
import com.janginharou.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 50)
    private String paymentMethod; // CARD, TRANSFER 등 (Mock용)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(length = 200)
    private String paymentKey; // Mock용 고유 키

    @Column(length = 200)
    private String orderId; // Mock용 주문 ID

    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime refundedAt;

    @Column(columnDefinition = "TEXT")
    private String cancelReason;

    public void complete() {
        this.status = PaymentStatus.COMPLETED;
        this.paidAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        this.status = PaymentStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancelReason = reason;
    }

    public void refund(String reason) {
        this.status = PaymentStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
        this.cancelReason = reason;
    }
}
