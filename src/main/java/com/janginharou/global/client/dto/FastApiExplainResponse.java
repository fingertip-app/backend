package com.janginharou.global.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FastApiExplainResponse {

    private String answer;
    private List<FastApiSourceResponse> sources;
    private List<String> matchingKeywords;
    private List<String> recommendedCategories;
}
