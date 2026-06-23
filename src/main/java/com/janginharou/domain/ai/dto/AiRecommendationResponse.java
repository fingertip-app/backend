package com.janginharou.domain.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class AiRecommendationResponse {

    private String answer;
    private List<ExplainSourceResponse> sources;
    private List<String> matchingKeywords;
    private List<String> recommendedTags;
    private List<RecommendedExperienceResponse> recommendedExperiences;

    @Schema(description = "AI 실패 시 기본 활성 체험 fallback 여부")
    private boolean fallback;

    @Schema(description = "fallback 안내 문구. fallback=false면 null")
    private String message;
}
