package com.janginharou.global.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FastApiSourceResponse {

    private Long id;
    private String name;
    private String source;
    private String category;
}
