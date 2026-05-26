package com.janginharou.domain.reservation.controller;

import com.janginharou.domain.reservation.dto.ReservationRequest;
import com.janginharou.domain.reservation.dto.ReservationResponse;
import com.janginharou.domain.reservation.entity.ReservationStatus;
import com.janginharou.domain.reservation.service.ReservationService;
import com.janginharou.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Tag(name = "Booking API", description = "예약 관련 API")
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    @Operation(summary = "내 예약 목록", description = "사용자의 예약 목록 조회")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getReservations(
            @RequestParam Long userId,
            @RequestParam(required = false) ReservationStatus status) {
        List<ReservationResponse> responses = (status == null
                ? reservationService.getReservationsByUserId(userId)
                : reservationService.getReservationsByUserIdAndStatus(userId, status))
                .stream()
                .map(ReservationResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

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
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(@RequestBody ReservationRequest request) {
        // TODO: 현재 로그인 사용자 ID 추출 후 예약 생성
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(null, "Reservation created successfully"));
    }

    @PostMapping("/{bookingId}/approve")
    @Operation(summary = "예약 승인", description = "예약 승인 (장인용)")
    public ResponseEntity<ApiResponse<ReservationResponse>> approveReservation(@PathVariable Long reservationId) {
        // TODO: 장인 권한 확인 후 예약 승인 (PENDING → APPROVED)
        return ResponseEntity.ok(ApiResponse.ok(null, "Reservation approved successfully"));
    }

    @PostMapping("/{bookingId}/reject")
    @Operation(summary = "예약 거절", description = "예약 거절 (장인용)")
    public ResponseEntity<ApiResponse<ReservationResponse>> rejectReservation(
            @PathVariable Long reservationId,
            @RequestParam String rejectionReason) {
        // TODO: 장인 권한 확인 후 예약 거절 (PENDING → REJECTED)
        return ResponseEntity.ok(ApiResponse.ok(null, "Reservation rejected successfully"));
    }

    @PostMapping("/{reservationId}/payment")
    @Operation(summary = "결제 처리", description = "예약 결제 처리 (토스페이먼츠)")
    public ResponseEntity<ApiResponse<ReservationResponse>> processPayment(
            @PathVariable Long reservationId,
            @RequestParam String paymentKey) {
        // TODO: 토스페이먼츠 웹훅 검증 후 결제 완료 처리 (APPROVED → PAID)
        return ResponseEntity.ok(ApiResponse.ok(null, "Payment processed successfully"));
    }

    @PostMapping("/{reservationId}/confirm")
    @Operation(summary = "예약 최종 확정", description = "예약 최종 확정")
    public ResponseEntity<ApiResponse<ReservationResponse>> confirmReservation(@PathVariable Long reservationId) {
        // TODO: 결제 완료 후 최종 확정 (PAID → CONFIRMED)
        return ResponseEntity.ok(ApiResponse.ok(null, "Reservation confirmed successfully"));
    }

    @PostMapping("/{reservationId}/cancel")
    @Operation(summary = "예약 취소", description = "예약 취소 및 환불")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelReservation(
            @PathVariable Long reservationId,
            @RequestParam(required = false) String cancellationReason) {
        // TODO: 예약 취소 처리 및 환불 처리
        return ResponseEntity.ok(ApiResponse.ok(null, "Reservation cancelled successfully"));
    }
}
