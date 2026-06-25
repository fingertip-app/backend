package com.janginharou.domain.payment.service;

import com.janginharou.domain.payment.entity.Payment;
import com.janginharou.domain.payment.repository.PaymentRepository;
import com.janginharou.domain.reservation.entity.Reservation;
import com.janginharou.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    /**
     * Mock 결제 생성 (공모전용 - 실제 PG 연동 없음)
     */
    @Transactional
    public Payment createMockPayment(Reservation reservation, String paymentMethod) {
        log.info("🔔 [Mock 결제] 생성 시작 - reservationId: {}, amount: {}", reservation.getId(), reservation.getTotalPrice());

        String mockPaymentKey = "mock_payment_" + UUID.randomUUID();
        String mockOrderId = "order_" + reservation.getId() + "_" + UUID.randomUUID();

        Payment payment = Payment.builder()
                .reservation(reservation)
                .amount(reservation.getTotalPrice())
                .paymentMethod(paymentMethod)
                .paymentKey(mockPaymentKey)
                .orderId(mockOrderId)
                .build();

        // Mock 결제는 바로 완료 처리
        payment.complete();

        Payment saved = paymentRepository.save(payment);
        log.info("✅ [Mock 결제] 완료 - paymentId: {}, paymentKey: {}", saved.getId(), saved.getPaymentKey());

        return saved;
    }

    @Transactional(readOnly = true)
    public Payment getPaymentByReservationId(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "reservationId", reservationId));
    }

    @Transactional
    public Payment cancelPayment(Long paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        payment.cancel(reason);
        log.info("✅ [결제 취소] paymentId: {}, reason: {}", paymentId, reason);

        return payment;
    }

    @Transactional
    public Payment refundPayment(Long paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        payment.refund(reason);
        log.info("✅ [결제 환불] paymentId: {}, reason: {}", paymentId, reason);

        return payment;
    }
}
