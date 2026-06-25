package com.janginharou.domain.qr.controller;

import com.janginharou.domain.qr.dto.QrVerifyRequest;
import com.janginharou.domain.qr.dto.QrVerifyResponse;
import com.janginharou.domain.qr.service.QrCodeService;
import com.janginharou.global.common.ApiResponse;
import com.janginharou.global.exception.InvalidRequestException;
import com.janginharou.global.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/qr")
@RequiredArgsConstructor
@Tag(name = "QR Code API", description = "QR 코드 생성/검증 API")
public class QrCodeController {

    private final QrCodeService qrCodeService;

    /**
     * 예약 ID로 QR 코드 조회 (체험자용)
     * GET /api/qr/reservation/{reservationId}
     */
    @GetMapping("/reservation/{reservationId}")
    @Operation(summary = "QR 코드 조회", description = "예약 ID로 QR 코드 조회")
    public ResponseEntity<ApiResponse<String>> getQrCode(@PathVariable Long reservationId) {
        try {
            String qrCode = qrCodeService.getQrCodeByReservationId(reservationId);
            return ResponseEntity.ok(ApiResponse.ok(qrCode));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                .body(ApiResponse.error("RESERVATION_NOT_FOUND", e.getMessage()));
        } catch (InvalidRequestException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(mapErrorCode(e.getMessage()), e.getMessage()));
        }
    }

    /**
     * QR 코드 검증 (장인용)
     * POST /api/qr/verify
     */
    @PostMapping("/verify")
    @Operation(summary = "QR 코드 검증", description = "QR 토큰으로 예약 정보 검증")
    public ResponseEntity<ApiResponse<QrVerifyResponse>> verifyQrCode(
            @RequestBody QrVerifyRequest request
    ) {
        try {
            QrVerifyResponse response = qrCodeService.verifyQrCode(request.getToken());
            return ResponseEntity.ok(ApiResponse.ok(response, "QR code verified successfully"));
        } catch (InvalidRequestException e) {
            String errorCode = mapErrorCode(e.getMessage());
            log.warn("QR verification failed: {} (code: {})", e.getMessage(), errorCode);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(errorCode, e.getMessage()));
        }
    }

    /**
     * QR 코드 재생성 (결제 완료 후 생성 실패 시 복구용)
     * POST /api/qr/regenerate/{reservationId}
     */
    @PostMapping("/regenerate/{reservationId}")
    @Operation(summary = "QR 코드 재생성", description = "결제 완료 후 QR 생성 실패 시 재생성")
    public ResponseEntity<ApiResponse<String>> regenerateQrCode(@PathVariable Long reservationId) {
        try {
            String qrCode = qrCodeService.regenerateQrCode(reservationId);
            return ResponseEntity.ok(ApiResponse.ok(qrCode, "QR code regenerated successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                .body(ApiResponse.error("RESERVATION_NOT_FOUND", e.getMessage()));
        } catch (InvalidRequestException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(mapErrorCode(e.getMessage()), e.getMessage()));
        }
    }

    private String mapErrorCode(String message) {
        if (message.contains("Payment")) {
            return "QR_NOT_PAID";
        }
        if (message.contains("expired")) {
            return "QR_EXPIRED";
        }
        if (message.contains("not yet valid")) {
            return "QR_NOT_VALID_YET";
        }
        if (message.contains("pending")) {
            return "RESERVATION_PENDING";
        }
        if (message.contains("cancelled")) {
            return "RESERVATION_CANCELLED";
        }
        if (message.contains("already been completed")) {
            return "RESERVATION_COMPLETED";
        }
        if (message.contains("not generated yet")) {
            return "QR_NOT_GENERATED";
        }
        if (message.contains("Invalid QR code")) {
            return "QR_INVALID";
        }
        return "QR_VERIFICATION_FAILED";
    }
}
