package com.janginharou.domain.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExplainRequest {

    @NotBlank
    @Size(max = 500)
    private String query;

    @Size(max = 10)
    private String locale;
}
