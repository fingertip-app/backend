package com.janginharou.domain.cardnews.controller;

import com.janginharou.domain.cardnews.dto.CardNewsRequest;
import com.janginharou.domain.cardnews.dto.CardNewsResponse;
import com.janginharou.domain.cardnews.service.CardNewsService;
import com.janginharou.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/card-news")
@RequiredArgsConstructor
@Tag(name = "CardNews API", description = "한물결 카드뉴스 API")
public class CardNewsController {

    private final CardNewsService cardNewsService;

    @GetMapping("/{cardNewsId}")
    @Operation(summary = "카드뉴스 조회", description = "카드뉴스 ID로 조회")
    public ResponseEntity<ApiResponse<CardNewsResponse>> getCardNews(@PathVariable Long cardNewsId) {
        CardNewsResponse response = CardNewsResponse.from(cardNewsService.getCardNewsById(cardNewsId));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/active")
    @Operation(summary = "활성 카드뉴스 목록", description = "모든 활성 카드뉴스 조회")
    public ResponseEntity<ApiResponse<List<CardNewsResponse>>> getActiveCardNews() {
        List<CardNewsResponse> responses = cardNewsService.getActiveCardNews()
                .stream()
                .map(CardNewsResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/type/{contentType}")
    @Operation(summary = "유형별 카드뉴스", description = "콘텐츠 유형별 카드뉴스 조회 (k_drama, k_pop, k_anime, festival)")
    public ResponseEntity<ApiResponse<List<CardNewsResponse>>> getCardNewsByType(@PathVariable String contentType) {
        List<CardNewsResponse> responses = cardNewsService.getCardNewsByContentType(contentType)
                .stream()
                .map(CardNewsResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/tag/{tag}")
    @Operation(summary = "태그별 카드뉴스", description = "카테고리 태그별 카드뉴스 조회")
    public ResponseEntity<ApiResponse<List<CardNewsResponse>>> getCardNewsByTag(@PathVariable String tag) {
        List<CardNewsResponse> responses = cardNewsService.getCardNewsByTag(tag)
                .stream()
                .map(CardNewsResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @PostMapping
    @Operation(summary = "카드뉴스 생성", description = "새로운 카드뉴스 생성 (관리자용)")
    public ResponseEntity<ApiResponse<CardNewsResponse>> createCardNews(@RequestBody CardNewsRequest request) {
        // TODO: 관리자 권한 확인 후 카드뉴스 생성
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(null, "CardNews created successfully"));
    }

    @PutMapping("/{cardNewsId}")
    @Operation(summary = "카드뉴스 수정", description = "카드뉴스 수정 (관리자용)")
    public ResponseEntity<ApiResponse<CardNewsResponse>> updateCardNews(
            @PathVariable Long cardNewsId,
            @RequestBody CardNewsRequest request) {
        // TODO: 관리자 권한 확인 후 카드뉴스 수정
        return ResponseEntity.ok(ApiResponse.ok(null, "CardNews updated successfully"));
    }

    @DeleteMapping("/{cardNewsId}")
    @Operation(summary = "카드뉴스 삭제", description = "카드뉴스 삭제 (관리자용)")
    public ResponseEntity<ApiResponse<Void>> deleteCardNews(@PathVariable Long cardNewsId) {
        // TODO: 관리자 권한 확인 후 카드뉴스 삭제
        return ResponseEntity.ok(ApiResponse.ok(null, "CardNews deleted successfully"));
    }
}
