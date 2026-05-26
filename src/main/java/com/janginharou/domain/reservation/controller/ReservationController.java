package com.janginharou.domain.reservation.controller;

import com.janginharou.domain.reservation.dto.ReservationRequest;
import com.janginharou.domain.reservation.dto.ReservationResponse;
import com.janginharou.domain.reservation.service.ReservationService;
import com.janginharou.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking API", description = "예약 관련 API")
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping("/{bookingId}")
    @Operation(summary = "예약 조회", description = "예약 ID로 예약 정보 조회")
    public ResponseEntity<ApiResponse<ReservationResponse>> getBooking(@PathVariable Long bookingId) {
        ReservationResponse response = ReservationResponse.from(reservationService.getBookingById(bookingId));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자 예약 목록", description = "사용자의 모든 예약 조회")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getUserBookings(@PathVariable Long userId) {
        List<ReservationResponse> responses = reservationService.getBookingsByUserId(userId)
                .stream()
                .map(ReservationResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/experience/{experienceId}")
    @Operation(summary = "체험의 예약 목록", description = "특정 체험의 모든 예약 조회")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getExperienceBookings(@PathVariable Long experienceId) {
        List<ReservationResponse> responses = reservationService.getBookingsByExperienceId(experienceId)
                .stream()
                .map(ReservationResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @PostMapping
    @Operation(summary = "예약 생성", description = "새로운 예약 생성")
    public ResponseEntity<ApiResponse<ReservationResponse>> createBooking(@RequestBody ReservationRequest request) {
        // TODO: 현재 로그인 사용자 ID 추출 후 예약 생성
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(null, "Booking created successfully"));
    }

    @PostMapping("/{bookingId}/approve")
    @Operation(summary = "예약 승인", description = "예약 승인 (장인용)")
    public ResponseEntity<ApiResponse<ReservationResponse>> approveBooking(@PathVariable Long bookingId) {
        // TODO: 장인 권한 확인 후 예약 승인 (PENDING → APPROVED)
        return ResponseEntity.ok(ApiResponse.ok(null, "Booking approved successfully"));
    }

    @PostMapping("/{bookingId}/reject")
    @Operation(summary = "예약 거절", description = "예약 거절 (장인용)")
    public ResponseEntity<ApiResponse<ReservationResponse>> rejectBooking(@PathVariable Long bookingId) {
        // TODO: 장인 권한 확인 후 예약 거절 (PENDING → REJECTED)
        return ResponseEntity.ok(ApiResponse.ok(null, "Booking rejected successfully"));
    }

    @PostMapping("/{bookingId}/confirm")
    @Operation(summary = "예약 최종 확정", description = "예약 최종 확정 (APPROVED → CONFIRMED)")
    public ResponseEntity<ApiResponse<ReservationResponse>> confirmBooking(@PathVariable Long bookingId) {
        // TODO: 예약 최종 확정
        return ResponseEntity.ok(ApiResponse.ok(null, "Booking confirmed successfully"));
    }

    @PostMapping("/{bookingId}/cancel")
    @Operation(summary = "예약 취소", description = "예약 취소")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelBooking(@PathVariable Long bookingId) {
        // TODO: 예약 취소 처리
        return ResponseEntity.ok(ApiResponse.ok(null, "Booking cancelled successfully"));
    }
}
