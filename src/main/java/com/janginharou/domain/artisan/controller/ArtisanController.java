package com.janginharou.domain.artisan.controller;

import com.janginharou.domain.artisan.dto.ArtisanRequest;
import com.janginharou.domain.artisan.dto.ArtisanResponse;
import com.janginharou.domain.artisan.service.ArtisanService;
import com.janginharou.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/artisans")
@RequiredArgsConstructor
@Tag(name = "Artisan API", description = "장인 관련 API")
public class ArtisanController {

    private final ArtisanService artisanService;

    @GetMapping("/{artisanId}")
    @Operation(summary = "장인 조회", description = "장인 ID로 장인 정보 조회")
    public ResponseEntity<ApiResponse<ArtisanResponse>> getArtisan(@PathVariable Long artisanId) {
        ArtisanResponse response = ArtisanResponse.from(artisanService.getArtisanById(artisanId));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자 장인 정보 조회", description = "사용자 ID로 장인 정보 조회")
    public ResponseEntity<ApiResponse<ArtisanResponse>> getArtisanByUserId(@PathVariable Long userId) {
        ArtisanResponse response = ArtisanResponse.from(artisanService.getArtisanByUserId(userId));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/verified")
    @Operation(summary = "인증된 장인 목록", description = "인증된 장인 목록 조회")
    public ResponseEntity<ApiResponse<List<ArtisanResponse>>> getVerifiedArtisans() {
        List<ArtisanResponse> responses = artisanService.getVerifiedArtisans()
                .stream()
                .map(ArtisanResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @PostMapping
    @Operation(summary = "장인 등록", description = "새로운 장인 등록")
    public ResponseEntity<ApiResponse<ArtisanResponse>> createArtisan(@RequestBody ArtisanRequest request) {
        // TODO: 장인 가입 신청 처리
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(null, "Artisan application submitted successfully"));
    }

    @PutMapping("/{artisanId}")
    @Operation(summary = "장인 정보 수정", description = "장인 정보 수정")
    public ResponseEntity<ApiResponse<ArtisanResponse>> updateArtisan(
            @PathVariable Long artisanId,
            @RequestBody ArtisanRequest request) {
        // TODO: 장인 정보 수정 처리
        return ResponseEntity.ok(ApiResponse.ok(null, "Artisan updated successfully"));
    }

    @PostMapping("/{artisanId}/approve")
    @Operation(summary = "장인 인증 승인", description = "장인 인증 승인 (관리자용)")
    public ResponseEntity<ApiResponse<ArtisanResponse>> approveArtisan(@PathVariable Long artisanId) {
        // TODO: 관리자 권한 확인 후 장인 승인 처리
        return ResponseEntity.ok(ApiResponse.ok(null, "Artisan approved successfully"));
    }

    @PostMapping("/{artisanId}/reject")
    @Operation(summary = "장인 인증 거절", description = "장인 인증 거절 (관리자용)")
    public ResponseEntity<ApiResponse<Void>> rejectArtisan(
            @PathVariable Long artisanId,
            @RequestParam String rejectionReason) {
        // TODO: 관리자 권한 확인 후 장인 거절 처리
        return ResponseEntity.ok(ApiResponse.ok(null, "Artisan rejected successfully"));
    }
}
