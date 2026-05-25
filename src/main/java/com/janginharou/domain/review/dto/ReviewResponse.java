package com.janginharou.domain.review.dto;

import com.janginharou.domain.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private Long id;
    private Long userId;
    private Long experienceId;
    private Integer rating;
    private String content;
    private String imageUrl;
    private String newKnowledge;
    private Boolean isApproved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .experienceId(review.getExperience().getId())
                .rating(review.getRating())
                .content(review.getContent())
                .imageUrl(review.getImageUrl())
                .newKnowledge(review.getNewKnowledge())
                .isApproved(review.getIsApproved())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
