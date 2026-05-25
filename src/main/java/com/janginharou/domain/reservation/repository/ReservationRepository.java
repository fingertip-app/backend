package com.janginharou.domain.reservation.repository;

import com.janginharou.domain.reservation.entity.Reservation;
import com.janginharou.domain.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserId(Long userId);

    List<Reservation> findByExperienceId(Long experienceId);

    List<Reservation> findByStatus(ReservationStatus status);

    List<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus status);

    List<Reservation> findByExperienceIdAndStatus(Long experienceId, ReservationStatus status);

    Optional<Reservation> findByPaymentOrderId(String paymentOrderId);
}
