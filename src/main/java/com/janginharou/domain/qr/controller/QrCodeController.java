package com.janginharou.domain.qr.controller;

import com.janginharou.domain.qr.dto.QrVerifyRequest;
import com.janginharou.domain.qr.dto.QrVerifyResponse;
import com.janginharou.domain.qr.service.QrCodeService;
import com.janginharou.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        String qrCode = qrCodeService.getQrCodeByReservationId(reservationId);
        return ResponseEntity.ok(ApiResponse.ok(qrCode));
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
        QrVerifyResponse response = qrCodeService.verifyQrCode(request.getToken());
        return ResponseEntity.ok(ApiResponse.ok(response, "QR code verified successfully"));
    }
}
