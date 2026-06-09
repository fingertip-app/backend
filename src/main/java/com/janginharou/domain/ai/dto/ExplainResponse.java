package com.janginharou.domain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ExplainResponse {

    private String answer;
    private List<ExplainSourceResponse> sources;
    private List<String> matchingKeywords;
    private List<String> recommendedCategories;
    private List<String> recommendedTags;
    private List<RelatedExperienceResponse> relatedExperiences;

    @Schema(description = "Spring이 합성 fallback 응답을 반환했는지 여부", example = "false")
    private boolean fallback;

    @Schema(description = "Spring 합성 fallback 안내 문구. fallback이 false이면 null")
    private String message;
}
