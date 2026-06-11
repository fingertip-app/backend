package com.janginharou.domain.reservation.service;

import com.janginharou.domain.experience.entity.Experience;
import com.janginharou.domain.experience.entity.ExperienceSchedule;
import com.janginharou.domain.experience.repository.ExperienceRepository;
import com.janginharou.domain.experience.repository.ExperienceScheduleRepository;
import com.janginharou.domain.reservation.dto.ReservationRequest;
import com.janginharou.domain.reservation.entity.Reservation;
import com.janginharou.domain.reservation.entity.ReservationStatus;
import com.janginharou.domain.reservation.repository.ReservationRepository;
import com.janginharou.domain.user.entity.User;
import com.janginharou.domain.user.repository.UserRepository;
import com.janginharou.global.exception.InvalidRequestException;
import com.janginharou.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ExperienceRepository experienceRepository;
    private final ExperienceScheduleRepository experienceScheduleRepository;

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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Experience experience = experienceRepository.findById(request.getExperienceId())
                .orElseThrow(() -> new ResourceNotFoundException("Experience", "id", request.getExperienceId()));
        ExperienceSchedule schedule = experienceScheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("ExperienceSchedule", "id", request.getScheduleId()));

        validateReservableExperience(experience, schedule);
        validateDuplicateReservation(userId, schedule.getId());
        validateCapacity(schedule, request.getNumberOfParticipants());

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
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation approveReservation(Long reservationId) {
        Reservation reservation = getReservationById(reservationId);
        validateStatus(reservation, ReservationStatus.PENDING);
        reservation.approve();
        return reservation;
    }

    @Transactional
    public Reservation rejectReservation(Long reservationId, String rejectionReason) {
        Reservation reservation = getReservationById(reservationId);
        validateStatus(reservation, ReservationStatus.PENDING);
        reservation.reject(rejectionReason);
        return reservation;
    }

    @Transactional
    public Reservation processPayment(Long reservationId, String paymentKey) {
        Reservation reservation = getReservationById(reservationId);
        validateStatus(reservation, ReservationStatus.APPROVED);
        reservation.pay(paymentKey, createPaymentOrderId(reservation));
        return reservation;
    }

    @Transactional
    public Reservation confirmReservation(Long reservationId) {
        Reservation reservation = getReservationById(reservationId);
        validateStatus(reservation, ReservationStatus.PAID);
        reservation.confirm();
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
        reservation.cancel(cancellationReason);
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

    private void validateReservableExperience(Experience experience, ExperienceSchedule schedule) {
        if (!Boolean.TRUE.equals(experience.getIsActive())) {
            throw new InvalidRequestException("Experience is not active");
        }
        if (!schedule.getExperience().getId().equals(experience.getId())) {
            throw new InvalidRequestException("Schedule does not belong to the experience");
        }
        if (!Boolean.TRUE.equals(schedule.getIsActive())) {
            throw new InvalidRequestException("Schedule is closed");
        }
        if (schedule.getScheduledAt().isBefore(LocalDateTime.now())) {
            throw new InvalidRequestException("Reserved date time has already passed");
        }
    }

    private void validateDuplicateReservation(Long userId, Long scheduleId) {
        if (reservationRepository.existsByUserIdAndScheduleIdAndStatusIn(
                userId,
                scheduleId,
                List.copyOf(ACTIVE_STATUSES)
        )) {
            throw new InvalidRequestException("Duplicate active reservation exists");
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
