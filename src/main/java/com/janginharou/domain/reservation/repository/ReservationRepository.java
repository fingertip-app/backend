package com.janginharou.domain.reservation.repository;

import com.janginharou.domain.reservation.entity.Booking;
import com.janginharou.domain.reservation.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    List<Booking> findByExperienceId(Long experienceId);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);

    List<Booking> findByExperienceIdAndStatus(Long experienceId, BookingStatus status);
}
