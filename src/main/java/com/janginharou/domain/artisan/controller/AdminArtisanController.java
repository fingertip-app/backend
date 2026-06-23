package com.janginharou.domain.artisan.controller;

import com.janginharou.domain.artisan.dto.ArtisanResponse;
import com.janginharou.domain.artisan.entity.ArtisanVerificationStatus;
import com.janginharou.domain.artisan.service.ArtisanService;
import com.janginharou.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/artisans")
@RequiredArgsConstructor
@Tag(name = "Admin Artisan API", description = "관리자 장인 인증 심사 API")
public class AdminArtisanController {

    private final ArtisanService artisanService;

    @GetMapping
    @Operation(summary = "장인 신청 목록", description = "인증 상태별 장인 목록 조회")
    public ResponseEntity<ApiResponse<List<ArtisanResponse>>> getArtisans(
            @RequestParam(required = false) ArtisanVerificationStatus certificationStatus
    ) {
        List<ArtisanResponse> responses = artisanService.getArtisansByStatus(certificationStatus)
                .stream()
                .map(ArtisanResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{artisanId}")
    @Operation(summary = "장인 신청 상세", description = "관리자 장인 신청 상세 조회")
    public ResponseEntity<ApiResponse<ArtisanResponse>> getArtisan(@PathVariable Long artisanId) {
        return ResponseEntity.ok(ApiResponse.ok(ArtisanResponse.from(artisanService.getArtisanById(artisanId))));
    }

    @PostMapping("/{artisanId}/approve")
    @Operation(summary = "장인 인증 승인", description = "장인 인증 상태를 approved로 변경")
    public ResponseEntity<ApiResponse<ArtisanResponse>> approve(@PathVariable Long artisanId) {
        return ResponseEntity.ok(ApiResponse.ok(ArtisanResponse.from(artisanService.approveArtisan(artisanId))));
    }

    @PostMapping("/{artisanId}/reject")
    @Operation(summary = "장인 인증 거절", description = "장인 인증 상태를 rejected로 변경")
    public ResponseEntity<ApiResponse<ArtisanResponse>> reject(@PathVariable Long artisanId) {
        return ResponseEntity.ok(ApiResponse.ok(ArtisanResponse.from(artisanService.rejectArtisan(artisanId))));
    }
}
