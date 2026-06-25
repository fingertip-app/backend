package com.janginharou.domain.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryRequest {

    @NotBlank(message = "리뷰 내용은 필수입니다")
    @Size(min = 1, max = 5000, message = "리뷰 내용은 1자 이상 5000자 이하여야 합니다")
    private String content;

    private String locale = "ko";
}
