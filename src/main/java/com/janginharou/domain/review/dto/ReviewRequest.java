package com.janginharou.domain.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequest {

    @NotNull
    private Long experienceId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    private String content;
    private String imageUrl;
    private String newKnowledge;
}
