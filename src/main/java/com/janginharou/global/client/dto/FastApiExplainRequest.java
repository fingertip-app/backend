package com.janginharou.global.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FastApiExplainRequest {

    private final String query;
    private final String locale;
}
