package com.janginharou.global.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FastApiRecommendationResponse {
    private String answer;
    private List<Long> recommendedExperienceIds;
    private Map<String, String> reasons;
    private List<String> matchingKeywords;
    private List<FastApiSourceResponse> sources;
    private Boolean fallback;
    private String message;
    private Map<String, Object> appliedFilters;
    private Map<String, List<Map<String, Object>>> connectionEvidence;

    public FastApiRecommendationResponse(String answer, List<Long> recommendedExperienceIds,
            Map<String, String> reasons, List<String> matchingKeywords,
            List<FastApiSourceResponse> sources, Boolean fallback, String message) {
        this(answer, recommendedExperienceIds, reasons, matchingKeywords, sources,
                fallback, message, Map.of(), Map.of());
    }
}
