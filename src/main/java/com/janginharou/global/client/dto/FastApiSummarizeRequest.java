package com.janginharou.global.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FastApiSummarizeRequest {

    private final String content;
    private final String locale;
}
