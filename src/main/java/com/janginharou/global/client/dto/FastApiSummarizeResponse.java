package com.janginharou.global.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FastApiSummarizeResponse {

    private String summary;
    private BigDecimal sentimentScore;
    private List<String> keywords;
}
