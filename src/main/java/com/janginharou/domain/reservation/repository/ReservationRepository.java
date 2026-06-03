package com.janginharou.domain.reservation.repository;

import com.janginharou.domain.reservation.entity.Reservation;
import com.janginharou.domain.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    boolean existsByUserIdAndExperienceIdAndStatusIn(Long userId, Long experienceId, List<ReservationStatus> statuses);

    @Query("""
            select coalesce(sum(r.numberOfParticipants), 0)
            from Reservation r
            where r.experience.id = :experienceId
              and r.status in :statuses
            """)
    Integer sumParticipantsByExperienceIdAndStatusIn(
            @Param("experienceId") Long experienceId,
            @Param("statuses") List<ReservationStatus> statuses
    );

    Optional<Reservation> findByPaymentOrderId(String paymentOrderId);
}