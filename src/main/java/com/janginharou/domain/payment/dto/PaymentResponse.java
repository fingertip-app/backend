package com.janginharou.domain.payment.dto;

import com.janginharou.domain.payment.entity.Payment;
import com.janginharou.domain.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long reservationId,
        BigDecimal amount,
        String paymentMethod,
        PaymentStatus status,
        String paymentKey,
        String orderId,
        LocalDateTime paidAt,
        LocalDateTime createdAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getReservation().getId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getPaymentKey(),
                payment.getOrderId(),
                payment.getPaidAt(),
                payment.getCreatedAt()
        );
    }
}
