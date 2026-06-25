package com.janginharou.domain.reservation.repository;

import com.janginharou.domain.reservation.entity.Reservation;
import com.janginharou.domain.reservation.entity.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
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

    void deleteByExperienceId(Long experienceId);

    List<Reservation> findByStatus(ReservationStatus status);

    List<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus status);

    List<Reservation> findByExperienceIdAndStatus(Long experienceId, ReservationStatus status);

    boolean existsByUserIdAndExperienceIdAndStatusIn(Long userId, Long experienceId, List<ReservationStatus> statuses);

    Optional<Reservation> findFirstByUserIdAndExperienceIdAndStatusInOrderByCreatedAtDesc(
            Long userId,
            Long experienceId,
            List<ReservationStatus> statuses
    );

    boolean existsByUserIdAndScheduleIdAndStatusIn(Long userId, Long scheduleId, List<ReservationStatus> statuses);

    boolean existsByExperienceIdAndStatusIn(Long experienceId, List<ReservationStatus> statuses);

    @Query("""
            select coalesce(sum(r.numberOfParticipants), 0)
            from Reservation r
            where r.schedule.id = :scheduleId
              and r.status in :statuses
            """)
    Integer sumParticipantsByScheduleIdAndStatusIn(
            @Param("scheduleId") Long scheduleId,
            @Param("statuses") List<ReservationStatus> statuses
    );

    Optional<Reservation> findByPaymentOrderId(String paymentOrderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Reservation> findByQrCode(String qrCode);

    /**
     * 정산되지 않은 완료 예약 조회 (정산 배치용)
     */
    @Query("""
            SELECT r FROM Reservation r
            WHERE r.status = :status
              AND r.updatedAt >= :startDateTime
              AND r.updatedAt < :endDateTime
              AND NOT EXISTS (
                SELECT 1 FROM SettlementItem si
                WHERE si.reservation.id = r.id
              )
            """)
    List<Reservation> findUnsettledReservations(
            @Param("status") ReservationStatus status,
            @Param("startDateTime") java.time.LocalDateTime startDateTime,
            @Param("endDateTime") java.time.LocalDateTime endDateTime
    );

    /**
     * 여러 체험의 특정 상태 예약 개수를 한 번에 조회 (N+1 방지)
     */
    @Query("""
            SELECT r.experience.id as experienceId, COUNT(r) as count
            FROM Reservation r
            WHERE r.experience.id IN :experienceIds
              AND r.status = :status
            GROUP BY r.experience.id
            """)
    List<Object[]> countByExperienceIdsAndStatus(
            @Param("experienceIds") List<Long> experienceIds,
            @Param("status") ReservationStatus status
    );
}
