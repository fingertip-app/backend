package com.janginharou.domain.cardnews.controller;

import com.janginharou.domain.cardnews.dto.CardNewsRequest;
import com.janginharou.domain.cardnews.dto.CardNewsResponse;
import com.janginharou.domain.cardnews.service.CardNewsService;
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
@RequestMapping("/card-news")
@RequiredArgsConstructor
@Tag(name = "CardNews API", description = "한물결 카드뉴스 API")
public class CardNewsController {

    private final CardNewsService cardNewsService;

    @GetMapping("/{cardNewsId}")
    @Operation(summary = "카드뉴스 조회", description = "카드뉴스 ID로 조회")
    public ResponseEntity<ApiResponse<CardNewsResponse>> getCardNews(@PathVariable Long cardNewsId) {
        return ResponseEntity.ok(ApiResponse.ok(cardNewsService.getCardNewsResponseById(cardNewsId)));
    }

    @GetMapping("/active")
    @Operation(summary = "활성 카드뉴스 목록", description = "모든 활성 카드뉴스 조회")
    public ResponseEntity<ApiResponse<List<CardNewsResponse>>> getActiveCardNews() {
        return ResponseEntity.ok(ApiResponse.ok(cardNewsService.getActiveCardNews()));
    }

    @GetMapping("/type/{contentType}")
    @Operation(summary = "유형별 카드뉴스", description = "콘텐츠 유형별 카드뉴스 조회 (k_drama, k_pop, k_anime, festival)")
    public ResponseEntity<ApiResponse<List<CardNewsResponse>>> getCardNewsByType(@PathVariable String contentType) {
        return ResponseEntity.ok(ApiResponse.ok(cardNewsService.getCardNewsByContentType(contentType)));
    }

    @GetMapping("/tag/{tag}")
    @Operation(summary = "태그별 카드뉴스", description = "카테고리 태그별 카드뉴스 조회")
    public ResponseEntity<ApiResponse<List<CardNewsResponse>>> getCardNewsByTag(@PathVariable String tag) {
        return ResponseEntity.ok(ApiResponse.ok(cardNewsService.getCardNewsByTag(tag)));
    }

    @PostMapping
    @Operation(summary = "카드뉴스 생성", description = "새로운 카드뉴스 생성 (관리자용)")
    public ResponseEntity<ApiResponse<CardNewsResponse>> createCardNews(@Valid @RequestBody CardNewsRequest request) {
        CardNewsResponse response = cardNewsService.createCardNews(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "CardNews created successfully"));
    }

    @PutMapping("/{cardNewsId}")
    @Operation(summary = "카드뉴스 수정", description = "카드뉴스 수정 (관리자용)")
    public ResponseEntity<ApiResponse<CardNewsResponse>> updateCardNews(
            @PathVariable Long cardNewsId,
            @Valid @RequestBody CardNewsRequest request) {
        CardNewsResponse response = cardNewsService.updateCardNews(cardNewsId, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "CardNews updated successfully"));
    }

    @DeleteMapping("/{cardNewsId}")
    @Operation(summary = "카드뉴스 삭제", description = "카드뉴스 삭제 (관리자용)")
    public ResponseEntity<ApiResponse<Void>> deleteCardNews(@PathVariable Long cardNewsId) {
        cardNewsService.deleteCardNews(cardNewsId);
        return ResponseEntity.ok(ApiResponse.ok(null, "CardNews deleted successfully"));
    }
}
