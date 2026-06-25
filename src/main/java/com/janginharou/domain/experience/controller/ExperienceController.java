package com.janginharou.domain.experience.controller;

import com.janginharou.domain.experience.dto.AddExperienceImagesRequest;
import com.janginharou.domain.experience.dto.ExperienceRequest;
import com.janginharou.domain.experience.dto.ExperienceResponse;
import com.janginharou.domain.experience.service.ExperienceService;
import com.janginharou.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/experiences")
@RequiredArgsConstructor
@Tag(name = "Experience API", description = "체험 프로그램 관련 API")
public class ExperienceController {

    private final ExperienceService experienceService;

    @GetMapping
    @Operation(summary = "체험 프로그램 목록", description = "인증된 사용자의 활성 체험 프로그램 목록 조회")
    public ResponseEntity<ApiResponse<List<ExperienceResponse>>> getExperiences() {
        return ResponseEntity.ok(ApiResponse.ok(experienceService.getActiveExperienceResponses()));
    }

    @GetMapping("/{experienceId}")
    @Operation(summary = "체험 프로그램 조회", description = "체험 프로그램 ID로 상세 정보 조회")
    public ResponseEntity<ApiResponse<ExperienceResponse>> getExperience(@PathVariable Long experienceId) {
        ExperienceResponse response = experienceService.getExperienceDetail(experienceId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/artisan/{artisanId}")
    @Operation(summary = "장인의 체험 프로그램 목록", description = "특정 장인의 모든 체험 프로그램 조회")
    public ResponseEntity<ApiResponse<List<ExperienceResponse>>> getExperiencesByArtisan(@PathVariable Long artisanId) {
        return ResponseEntity.ok(ApiResponse.ok(experienceService.getExperienceResponsesByArtisanId(artisanId)));
    }

    @GetMapping("/active")
    @Operation(summary = "활성 체험 프로그램 목록", description = "활성 체험 프로그램 전체 목록 조회")
    public ResponseEntity<ApiResponse<List<ExperienceResponse>>> getActiveExperiences() {
        return ResponseEntity.ok(ApiResponse.ok(experienceService.getActiveExperienceResponses()));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "예정된 체험 프로그램 목록", description = "현재 이후의 예정된 체험 프로그램 조회")
    public ResponseEntity<ApiResponse<List<ExperienceResponse>>> getUpcomingExperiences() {
        return ResponseEntity.ok(ApiResponse.ok(experienceService.getUpcomingExperiences()));
    }

    @PostMapping
    @Operation(summary = "체험 프로그램 생성", description = "새로운 체험 프로그램 생성")
    public ResponseEntity<ApiResponse<ExperienceResponse>> createExperience(
            @RequestParam Long artisanId,
            @Valid @RequestBody ExperienceRequest request) {
        ExperienceResponse response = experienceService.createExperience(artisanId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Experience created successfully"));
    }

    @PutMapping("/{experienceId}")
    @Operation(summary = "체험 프로그램 수정", description = "체험 프로그램 정보 수정")
    public ResponseEntity<ApiResponse<ExperienceResponse>> updateExperience(
            @PathVariable Long experienceId,
            @RequestParam Long artisanId,
            @Valid @RequestBody ExperienceRequest request) {
        ExperienceResponse response = experienceService.updateExperience(experienceId, artisanId, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Experience updated successfully"));
    }

    @PostMapping("/{experienceId}/images")
    @Operation(summary = "泥댄뿕 ?대?吏 異붽?", description = "泥댄뿕 ?곸꽭 ?대?吏瑜?湲곗〈 ?대?吏 ?ㅼ쓬 ?쒖꽌濡?異붽?")
    public ResponseEntity<ApiResponse<ExperienceResponse>> addExperienceImages(
            @PathVariable Long experienceId,
            @RequestParam Long artisanId,
            @Valid @RequestBody AddExperienceImagesRequest request) {
        ExperienceResponse response = experienceService.addExperienceImages(experienceId, artisanId, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Experience images added successfully"));
    }

    @DeleteMapping("/{experienceId}")
    @Operation(summary = "체험 프로그램 삭제", description = "체험 프로그램 삭제")
    public ResponseEntity<ApiResponse<Void>> deleteExperience(
            @PathVariable Long experienceId,
            @RequestParam Long artisanId) {
        experienceService.deleteExperience(experienceId, artisanId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Experience deleted successfully"));
    }
}
