package com.janginharou.domain.review.dto;

import com.janginharou.domain.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private Long id;
    private Long bookingId;
    private Long userId;
    private Long experienceId;
    private Integer rating;
    private String content;
    private String newLearnings;
    private List<String> imageUrls;
    private BigDecimal sentimentScore;
    private List<String> keywords;
    private LocalDateTime createdAt;

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .bookingId(review.getBooking().getId())
                .userId(review.getUser().getId())
                .experienceId(review.getExperience().getId())
                .rating(review.getRating())
                .content(review.getContent())
                .newLearnings(review.getNewLearnings())
                .imageUrls(review.getImageUrls())
                .sentimentScore(review.getSentimentScore())
                .keywords(review.getKeywords())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
