package com.janginharou.domain.reservation.controller;

import com.janginharou.domain.reservation.dto.ReservationRequest;
import com.janginharou.domain.reservation.dto.ReservationResponse;
import com.janginharou.domain.reservation.entity.ReservationStatus;
import com.janginharou.domain.reservation.service.ReservationService;
import com.janginharou.global.common.ApiResponse;
import com.janginharou.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation API", description = "예약 관련 API")
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    @Operation(summary = "내 예약 목록", description = "인증된 사용자의 예약 목록 조회\n\nQuery Parameters:\n- include=experience: 체험 정보 포함 (선택사항)\n- status: 예약 상태 필터 (선택사항)")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getReservations(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) String include) {
        boolean includeExperience = "experience".equals(include);
        List<ReservationResponse> responses = (status == null
                ? reservationService.getReservationsByUserId(currentUser.id())
                : reservationService.getReservationsByUserIdAndStatus(currentUser.id(), status))
                .stream()
                .map(reservation -> reservationService.buildReservationResponse(reservation, includeExperience))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{reservationId}")
    @Operation(summary = "예약 조회", description = "예약 ID로 예약 정보 조회\n\nQuery Parameters:\n- include=experience: 체험 정보 포함 (선택사항)")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservation(
            @PathVariable Long reservationId,
            @RequestParam(required = false) String include) {
        boolean includeExperience = "experience".equals(include);
        ReservationResponse response = reservationService.buildReservationResponse(
                reservationService.getReservationById(reservationId),
                includeExperience
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자 예약 목록", description = "사용자의 모든 예약 조회\n\nQuery Parameters:\n- include=experience: 체험 정보 포함 (선택사항)")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getUserReservations(
            @PathVariable Long userId,
            @RequestParam(required = false) String include) {
        boolean includeExperience = "experience".equals(include);
        List<ReservationResponse> responses = reservationService.getReservationsByUserId(userId)
                .stream()
                .map(reservation -> reservationService.buildReservationResponse(reservation, includeExperience))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/experience/{experienceId}")
    @Operation(summary = "체험의 예약 목록", description = "특정 체험의 모든 예약 조회")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getExperienceReservations(@PathVariable Long experienceId) {
        List<ReservationResponse> responses = reservationService.getReservationsByExperienceId(experienceId)
                .stream()
                .map(ReservationResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @PostMapping
    @Operation(summary = "예약 생성", description = "새로운 예약 생성")
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody ReservationRequest request) {
        log.info("🔔 [컨트롤러] POST /reservations 요청 받음 - userId: {}, request: {}", currentUser.id(), request);

        ReservationResponse response = ReservationResponse.from(reservationService.createReservation(currentUser.id(), request));
        log.info("✅ [컨트롤러] 예약 생성 완료 - reservationId: {}", response.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Reservation created successfully"));
    }

    @PostMapping("/{reservationId}/approve")
    @Operation(summary = "예약 승인", description = "예약 승인 (장인용)")
    public ResponseEntity<ApiResponse<ReservationResponse>> approveReservation(
            @PathVariable Long reservationId,
            @RequestParam(required = false) Long artisanId) {
        ReservationResponse response = ReservationResponse.from(
                artisanId == null
                        ? reservationService.approveReservation(reservationId)
                        : reservationService.approveReservation(reservationId, artisanId)
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "Reservation approved successfully"));
    }

    @PostMapping("/{reservationId}/reject")
    @Operation(summary = "예약 거절", description = "예약 거절 (장인용)")
    public ResponseEntity<ApiResponse<ReservationResponse>> rejectReservation(
            @PathVariable Long reservationId,
            @RequestParam(required = false) Long artisanId,
            @RequestParam String rejectionReason) {
        ReservationResponse response = ReservationResponse.from(
                artisanId == null
                        ? reservationService.rejectReservation(reservationId, rejectionReason)
                        : reservationService.rejectReservation(reservationId, artisanId, rejectionReason)
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "Reservation rejected successfully"));
    }

    @PostMapping("/{reservationId}/payment")
    @Operation(summary = "결제 처리", description = "예약 결제 처리 (토스페이먼츠)")
    public ResponseEntity<ApiResponse<ReservationResponse>> processPayment(
            @PathVariable Long reservationId,
            @RequestParam String paymentKey) {
        ReservationResponse response = ReservationResponse.from(reservationService.processPayment(reservationId, paymentKey));
        return ResponseEntity.ok(ApiResponse.ok(response, "Payment processed successfully"));
    }

    @PostMapping("/{reservationId}/confirm")
    @Operation(summary = "예약 최종 확정", description = "예약 최종 확정")
    public ResponseEntity<ApiResponse<ReservationResponse>> confirmReservation(@PathVariable Long reservationId) {
        ReservationResponse response = ReservationResponse.from(reservationService.confirmReservation(reservationId));
        return ResponseEntity.ok(ApiResponse.ok(response, "Reservation confirmed successfully"));
    }

    @PostMapping("/{reservationId}/cancel")
    @Operation(summary = "예약 취소", description = "예약 취소 및 환불")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelReservation(
            @PathVariable Long reservationId,
            @RequestParam(required = false) String cancellationReason) {
        ReservationResponse response = ReservationResponse.from(
                reservationService.cancelReservation(reservationId, cancellationReason)
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "Reservation cancelled successfully"));
    }
}
