package com.janginharou.domain.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReplyRequest {

    @NotBlank(message = "답글 내용을 입력해주세요")
    @Size(max = 1000, message = "답글은 1000자 이내로 작성해주세요")
    private String replyContent;
}
