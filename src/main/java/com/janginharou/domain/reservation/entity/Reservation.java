package com.janginharou.domain.reservation.entity;

import com.janginharou.domain.experience.entity.Experience;
import com.janginharou.domain.user.entity.User;
import com.janginharou.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "experience_id", nullable = false)
    private Experience experience;

    @Column(nullable = false)
    private Integer numberOfParticipants;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    private LocalDateTime reservedDateTime;

    private String paymentKey;
    private String paymentOrderId;

    private String rejectionReason;
    private String cancellationReason;
    private String requestMessage;

    @Column(nullable = false)
    private Boolean isNotificationSent;

    public void approve() {
        this.status = ReservationStatus.APPROVED;
    }

    public void pay(String paymentKey, String paymentOrderId) {
        this.status = ReservationStatus.PAID;
        this.paymentKey = paymentKey;
        this.paymentOrderId = paymentOrderId;
    }

    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
    }

    public void reject(String rejectionReason) {
        this.status = ReservationStatus.REJECTED;
        this.rejectionReason = rejectionReason;
    }

    public void cancel(String cancellationReason) {
        this.status = ReservationStatus.CANCELLED;
        this.cancellationReason = cancellationReason;
    }
}
