package com.janginharou.domain.reservation.service;

import com.janginharou.domain.reservation.entity.Reservation;
import com.janginharou.domain.reservation.entity.ReservationStatus;
import com.janginharou.domain.reservation.repository.ReservationRepository;
import com.janginharou.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public Reservation getReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByUserId(Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByExperienceId(Long experienceId) {
        return reservationRepository.findByExperienceId(experienceId);
    }

    @Transactional
    public Reservation createReservation(Reservation reservation) {
        // TODO: 예약 생성 처리 (상태는 PENDING으로, 장인에게 알림 전송)
        reservation = Reservation.builder()
                .user(reservation.getUser())
                .experience(reservation.getExperience())
                .numberOfParticipants(reservation.getNumberOfParticipants())
                .totalPrice(reservation.getTotalPrice())
                .status(ReservationStatus.PENDING)
                .isNotificationSent(false)
                .build();
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation approveReservation(Long reservationId) {
        // TODO: 장인이 예약 승인 (PENDING → APPROVED)
        Reservation reservation = getReservationById(reservationId);
        return reservation;
    }

    @Transactional
    public Reservation rejectReservation(Long reservationId, String rejectionReason) {
        // TODO: 장인이 예약 거절 (PENDING → REJECTED)
        Reservation reservation = getReservationById(reservationId);
        return reservation;
    }

    @Transactional
    public Reservation processPayment(Long reservationId, String paymentKey) {
        // TODO: 토스페이먼츠 웹훅 검증 후 예약 상태 PAID로 변경 (APPROVED → PAID)
        Reservation reservation = getReservationById(reservationId);
        return reservation;
    }

    @Transactional
    public Reservation confirmReservation(Long reservationId) {
        // TODO: 결제 완료 후 최종 확정 (PAID → CONFIRMED)
        Reservation reservation = getReservationById(reservationId);
        return reservation;
    }

    @Transactional
    public Reservation cancelReservation(Long reservationId, String cancellationReason) {
        // TODO: 예약 취소 처리 및 환불 처리 (모든 상태에서 CANCELLED 가능)
        Reservation reservation = getReservationById(reservationId);
        return reservation;
    }

    @Transactional(readOnly = true)
    public List<Reservation> getPendingReservations() {
        return reservationRepository.findByStatus(ReservationStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getConfirmedReservations() {
        return reservationRepository.findByStatus(ReservationStatus.CONFIRMED);
    }
}
