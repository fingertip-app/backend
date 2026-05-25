package com.janginharou.domain.review.controller;

import com.janginharou.domain.review.dto.ReviewRequest;
import com.janginharou.domain.review.dto.ReviewResponse;
import com.janginharou.domain.review.service.ReviewService;
import com.janginharou.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review API", description = "후기 관련 API")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/{reviewId}")
    @Operation(summary = "후기 조회", description = "후기 ID로 후기 정보 조회")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReview(@PathVariable Long reviewId) {
        ReviewResponse response = ReviewResponse.from(reviewService.getReviewById(reviewId));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/experience/{experienceId}")
    @Operation(summary = "체험의 모든 후기", description = "특정 체험의 모든 후기 조회")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByExperience(@PathVariable Long experienceId) {
        List<ReviewResponse> responses = reviewService.getReviewsByExperienceId(experienceId)
                .stream()
                .map(ReviewResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/experience/{experienceId}/approved")
    @Operation(summary = "체험의 승인된 후기", description = "특정 체험의 승인된 후기만 조회")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getApprovedReviewsByExperience(@PathVariable Long experienceId) {
        List<ReviewResponse> responses = reviewService.getApprovedReviewsByExperienceId(experienceId)
                .stream()
                .map(ReviewResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자의 후기 목록", description = "사용자가 작성한 모든 후기 조회")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByUser(@PathVariable Long userId) {
        List<ReviewResponse> responses = reviewService.getReviewsByUserId(userId)
                .stream()
                .map(ReviewResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @PostMapping
    @Operation(summary = "후기 작성", description = "새로운 후기 작성")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(@RequestBody ReviewRequest request) {
        // TODO: 현재 로그인 사용자 ID 추출 후 후기 생성
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(null, "Review created successfully"));
    }

    @PutMapping("/{reviewId}")
    @Operation(summary = "후기 수정", description = "후기 수정 (작성자만 가능)")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewRequest request) {
        // TODO: 후기 작성자 확인 후 수정 처리
        return ResponseEntity.ok(ApiResponse.ok(null, "Review updated successfully"));
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "후기 삭제", description = "후기 삭제 (작성자만 가능)")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long reviewId) {
        // TODO: 후기 작성자 확인 후 삭제 처리
        return ResponseEntity.ok(ApiResponse.ok(null, "Review deleted successfully"));
    }

    @PostMapping("/{reviewId}/approve")
    @Operation(summary = "후기 승인", description = "후기 승인 (관리자용)")
    public ResponseEntity<ApiResponse<ReviewResponse>> approveReview(@PathVariable Long reviewId) {
        // TODO: 관리자 권한 확인 후 후기 승인 처리
        return ResponseEntity.ok(ApiResponse.ok(null, "Review approved successfully"));
    }
}
