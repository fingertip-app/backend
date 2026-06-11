package com.janginharou.domain.ai.controller;

import com.janginharou.domain.ai.dto.AiRecommendationRequest;
import com.janginharou.domain.ai.dto.AiRecommendationResponse;
import com.janginharou.domain.ai.service.RecommendationService;
import com.janginharou.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Recommendation API", description = "취향 기반 AI 추천 API")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping("/recommendations")
    @Operation(
            summary = "취향 기반 체험 추천",
            description = "사용자의 취향에 기반한 체험 추천을 반환합니다. "
                    + "AI 실패 시 fallback=true로 기본 활성 체험을 제공합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "추천 성공 (또는 fallback 응답)",
                    content = @Content(
                            schema = @Schema(implementation = AiRecommendationResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Success",
                                      "data": {
                                        "answer": "친구와 함께할 수 있는 전통 공예 체험을 추천합니다.",
                                        "sources": [],
                                        "matchingKeywords": ["공예", "매듭"],
                                        "recommendedTags": ["공예", "매듭"],
                                        "recommendedExperiences": [
                                          {
                                            "id": 1,
                                            "title": "전통 매듭 만들기",
                                            "location": "서울 종로구",
                                            "price": 30000,
                                            "durationMinutes": 60,
                                            "tags": ["공예", "매듭"],
                                            "matchReason": "태그와 매칭"
                                          }
                                        ],
                                        "fallback": false,
                                        "message": null
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청값 검증 실패 또는 AI 요청 검증 실패"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "503",
                    description = "AI 서비스 사용 불가 (fallback 불가능한 경우)"
            )
    })
    public ResponseEntity<ApiResponse<AiRecommendationResponse>> recommend(
            @Valid @RequestBody AiRecommendationRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(recommendationService.recommend(request)));
    }
}
