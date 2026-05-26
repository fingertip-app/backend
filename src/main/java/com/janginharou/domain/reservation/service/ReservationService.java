package com.janginharou.domain.reservation.service;

import com.janginharou.domain.reservation.entity.Booking;
import com.janginharou.domain.reservation.entity.BookingStatus;
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
    public Booking getBookingById(Long bookingId) {
        return reservationRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsByUserId(Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsByExperienceId(Long experienceId) {
        return reservationRepository.findByExperienceId(experienceId);
    }

    @Transactional
    public Booking createBooking(Booking booking) {
        // TODO: 예약 생성 처리 (상태는 PENDING으로, 장인에게 알림 전송)
        return reservationRepository.save(booking);
    }

    @Transactional
    public Booking approveBooking(Long bookingId) {
        // TODO: 장인이 예약 승인 (PENDING → APPROVED)
        Booking booking = getBookingById(bookingId);
        return booking;
    }

    @Transactional
    public Booking rejectBooking(Long bookingId) {
        // TODO: 장인이 예약 거절 (PENDING → REJECTED)
        Booking booking = getBookingById(bookingId);
        return booking;
    }

    @Transactional
    public Booking confirmBooking(Long bookingId) {
        // TODO: 예약 최종 확정 (APPROVED → CONFIRMED)
        Booking booking = getBookingById(bookingId);
        return booking;
    }

    @Transactional
    public Booking cancelBooking(Long bookingId) {
        // TODO: 예약 취소 처리
        Booking booking = getBookingById(bookingId);
        return booking;
    }

    @Transactional(readOnly = true)
    public List<Booking> getPendingBookings() {
        return reservationRepository.findByStatus(BookingStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<Booking> getConfirmedBookings() {
        return reservationRepository.findByStatus(BookingStatus.CONFIRMED);
    }
}
