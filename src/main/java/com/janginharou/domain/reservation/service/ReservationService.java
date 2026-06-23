package com.janginharou.domain.reservation.service;

import com.janginharou.domain.experience.entity.Experience;
import com.janginharou.domain.experience.entity.ExperienceSchedule;
import com.janginharou.domain.experience.repository.ExperienceRepository;
import com.janginharou.domain.experience.repository.ExperienceScheduleRepository;
import com.janginharou.domain.reservation.dto.ReservationRequest;
import com.janginharou.domain.reservation.entity.Reservation;
import com.janginharou.domain.reservation.entity.ReservationStatus;
import com.janginharou.domain.reservation.event.ReservationStatusChangedEvent;
import com.janginharou.domain.reservation.repository.ReservationRepository;
import com.janginharou.domain.user.entity.User;
import com.janginharou.domain.user.repository.UserRepository;
import com.janginharou.global.exception.InvalidRequestException;
import com.janginharou.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ExperienceRepository experienceRepository;
    private final ExperienceScheduleRepository experienceScheduleRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final Set<ReservationStatus> ACTIVE_STATUSES = EnumSet.of(
            ReservationStatus.PENDING,
            ReservationStatus.APPROVED,
            ReservationStatus.PAID,
            ReservationStatus.CONFIRMED
    );

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
    public List<Reservation> getReservationsByUserIdAndStatus(Long userId, ReservationStatus status) {
        return reservationRepository.findByUserIdAndStatus(userId, status);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByExperienceId(Long experienceId) {
        return reservationRepository.findByExperienceId(experienceId);
    }

    @Transactional
    public Reservation createReservation(Long userId, ReservationRequest request) {
        log.info("🔔 [예약 생성] 시작 - userId: {}, request: {}", userId, request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        log.info("✅ [예약 생성] 사용자 조회 성공 - user: {}", user.getName());

        ExperienceSchedule schedule = experienceScheduleRepository.findByIdForUpdate(request.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("ExperienceSchedule", "id", request.getScheduleId()));
        log.info("✅ [예약 생성] 스케줄 조회 성공 - scheduleId: {}, scheduledAt: {}", schedule.getId(), schedule.getScheduledAt());

        Experience experience = schedule.getExperience();
        log.info("✅ [예약 생성] 체험 조회 성공 - experienceId: {}, title: {}", experience.getId(), experience.getTitle());

        validateScheduleMatchesExperience(schedule, request.getExperienceId());
        log.info("✅ [예약 생성] 체험-스케줄 매칭 검증 통과");

        validateReservableSchedule(schedule);
        log.info("✅ [예약 생성] 예약 가능 스케줄 검증 통과");

        validateDuplicateReservation(userId, schedule.getId());
        log.info("✅ [예약 생성] 중복 예약 검증 통과");

        validateCapacity(schedule, request.getNumberOfParticipants());
        log.info("✅ [예약 생성] 정원 검증 통과");

        BigDecimal totalPrice = experience.getPrice().multiply(BigDecimal.valueOf(request.getNumberOfParticipants()));

        Reservation reservation = Reservation.builder()
                .user(user)
                .experience(experience)
                .schedule(schedule)
                .numberOfParticipants(request.getNumberOfParticipants())
                .totalPrice(totalPrice)
                .status(ReservationStatus.PENDING)
                .reservedDateTime(schedule.getScheduledAt())
                .requestMessage(request.getRequestMessage())
                .isNotificationSent(false)
                .build();
        Reservation saved = reservationRepository.save(reservation);
        log.info("✅ [예약 생성] 저장 완료 - reservationId: {}, status: {}, totalPrice: {}", saved.getId(), saved.getStatus(), saved.getTotalPrice());
        return saved;
    }

    @Transactional
    public Reservation approveReservation(Long reservationId) {
        Reservation reservation = getReservationById(reservationId);
        validateStatus(reservation, ReservationStatus.PENDING);
        ReservationStatus oldStatus = reservation.getStatus();
        reservation.approve();
        publishStatusChangeEvent(reservation, oldStatus, null);
        return reservation;
    }

    @Transactional
    public Reservation approveReservation(Long reservationId, Long artisanId) {
        Reservation reservation = getReservationById(reservationId);
        validateArtisanOwnsReservation(reservation, artisanId);
        validateStatus(reservation, ReservationStatus.PENDING);
        ReservationStatus oldStatus = reservation.getStatus();
        reservation.approve();
        publishStatusChangeEvent(reservation, oldStatus, null);
        return reservation;
    }

    @Transactional
    public Reservation rejectReservation(Long reservationId, String rejectionReason) {
        Reservation reservation = getReservationById(reservationId);
        validateStatus(reservation, ReservationStatus.PENDING);
        ReservationStatus oldStatus = reservation.getStatus();
        reservation.reject(rejectionReason);
        publishStatusChangeEvent(reservation, oldStatus, rejectionReason);
        return reservation;
    }

    @Transactional
    public Reservation rejectReservation(Long reservationId, Long artisanId, String rejectionReason) {
        Reservation reservation = getReservationById(reservationId);
        validateArtisanOwnsReservation(reservation, artisanId);
        validateStatus(reservation, ReservationStatus.PENDING);
        ReservationStatus oldStatus = reservation.getStatus();
        reservation.reject(rejectionReason);
        publishStatusChangeEvent(reservation, oldStatus, rejectionReason);
        return reservation;
    }

    @Transactional
    public Reservation processPayment(Long reservationId, String paymentKey) {
        Reservation reservation = getReservationById(reservationId);
        validateStatus(reservation, ReservationStatus.APPROVED);
        ReservationStatus oldStatus = reservation.getStatus();
        reservation.pay(paymentKey, createPaymentOrderId(reservation));
        publishStatusChangeEvent(reservation, oldStatus, null);
        return reservation;
    }

    @Transactional
    public Reservation confirmReservation(Long reservationId) {
        Reservation reservation = getReservationById(reservationId);
        validateStatus(reservation, ReservationStatus.PAID);
        ReservationStatus oldStatus = reservation.getStatus();
        reservation.confirm();
        publishStatusChangeEvent(reservation, oldStatus, null);
        return reservation;
    }

    @Transactional
    public Reservation cancelReservation(Long reservationId, String cancellationReason) {
        Reservation reservation = getReservationById(reservationId);
        if (reservation.getStatus() == ReservationStatus.COMPLETED
                || reservation.getStatus() == ReservationStatus.REJECTED
                || reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new InvalidRequestException("Cannot cancel reservation in status: " + reservation.getStatus());
        }
        ReservationStatus oldStatus = reservation.getStatus();
        reservation.cancel(cancellationReason);
        publishStatusChangeEvent(reservation, oldStatus, cancellationReason);
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

    private void validateScheduleMatchesExperience(ExperienceSchedule schedule, Long experienceId) {
        if (!schedule.getExperience().getId().equals(experienceId)) {
            throw new InvalidRequestException("Schedule does not belong to experience");
        }
    }

    private void validateReservableSchedule(ExperienceSchedule schedule) {
        Experience experience = schedule.getExperience();
        if (!Boolean.TRUE.equals(experience.getIsActive())) {
            throw new InvalidRequestException("Experience is not active");
        }
        if (!Boolean.TRUE.equals(schedule.getIsActive())) {
            throw new InvalidRequestException("Schedule is not active");
        }
        if (schedule.getScheduledAt().isBefore(LocalDateTime.now())) {
            throw new InvalidRequestException("Schedule has already passed");
        }
    }

    private void validateDuplicateReservation(Long userId, Long scheduleId) {
        if (reservationRepository.existsByUserIdAndScheduleIdAndStatusIn(
                userId,
                scheduleId,
                List.copyOf(ACTIVE_STATUSES)
        )) {
            throw new InvalidRequestException("Duplicate active reservation exists for this schedule");
        }
    }

    private void validateCapacity(ExperienceSchedule schedule, Integer requestedParticipants) {
        Integer currentParticipants = reservationRepository.sumParticipantsByScheduleIdAndStatusIn(
                schedule.getId(),
                List.copyOf(ACTIVE_STATUSES)
        );
        if (currentParticipants + requestedParticipants > schedule.getAvailableSlots()) {
            throw new InvalidRequestException("Booking slot is unavailable");
        }
    }

    private void validateArtisanOwnsReservation(Reservation reservation, Long artisanId) {
        if (artisanId == null) {
            return;
        }
        if (!reservation.getExperience().getArtisan().getId().equals(artisanId)) {
            throw new InvalidRequestException("Reservation does not belong to artisan");
        }
    }

    private void publishStatusChangeEvent(Reservation reservation, ReservationStatus oldStatus, String reason) {
        ReservationStatusChangedEvent event = ReservationStatusChangedEvent.of(
                reservation.getId(),
                reservation.getUser(), // User 객체 직접 전달로 리스너에서 DB 조회 불필요
                reservation.getExperience().getId(),
                reservation.getExperience().getTitle(),
                oldStatus,
                reservation.getStatus(),
                reason
        );
        eventPublisher.publishEvent(event);
    }

    private void validateStatus(Reservation reservation, ReservationStatus expectedStatus) {
        if (reservation.getStatus() != expectedStatus) {
            throw new InvalidRequestException(
                    "Invalid reservation status. expected=" + expectedStatus + ", actual=" + reservation.getStatus()
            );
        }
    }

    private String createPaymentOrderId(Reservation reservation) {
        return "reservation-" + reservation.getId() + "-" + UUID.randomUUID();
    }
}
