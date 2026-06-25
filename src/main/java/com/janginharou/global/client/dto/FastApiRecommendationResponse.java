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
    private Boolean fallback;
    private String message;
}
