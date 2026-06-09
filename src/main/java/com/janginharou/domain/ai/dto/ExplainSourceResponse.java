package com.janginharou.domain.ai.dto;

import com.janginharou.global.client.dto.FastApiSourceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ExplainSourceResponse {

    private Long id;
    private String name;
    private String source;
    private String category;

    public static ExplainSourceResponse from(FastApiSourceResponse source) {
        return ExplainSourceResponse.builder()
                .id(source.getId())
                .name(source.getName())
                .source(source.getSource())
                .category(source.getCategory())
                .build();
    }
}
