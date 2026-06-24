package com.janginharou.domain.experience.dto;

import com.janginharou.domain.experience.entity.Experience;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 예약 응답에 포함될 체험 정보 (평점/리뷰 포함)
 * Frontend에서 체험 상세 화면으로 이동할 때 필요한 최소 정보
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceWithReviewsDto {

    private Long id;
    private String title;
    private String category;
    private String locationAddress;
    private BigDecimal price;
    private Integer durationMinutes;
    private String artisanName;
    private Double rating;
    private Long reviewCount;
    private Integer maxParticipants;

    /**
     * Experience 엔티티 + 평점 데이터로부터 생성
     */
    public static ExperienceWithReviewsDto from(Experience experience, Double rating, Long reviewCount) {
        return ExperienceWithReviewsDto.builder()
                .id(experience.getId())
                .title(experience.getTitle())
                .category(experience.getCategory())
                .locationAddress(experience.getLocationAddress())
                .price(experience.getPrice())
                .durationMinutes(experience.getDurationMinutes())
                .artisanName(experience.getArtisan() != null ? experience.getArtisan().getName() : null)
                .rating(rating != null ? rating : 0.0)
                .reviewCount(reviewCount != null ? reviewCount : 0L)
                .maxParticipants(experience.getMaxParticipants())
                .build();
    }
}
