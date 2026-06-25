package com.janginharou.domain.ai.dto;

import com.janginharou.domain.experience.entity.Experience;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecommendedExperienceResponse {

    private Long id;
    private String title;
    private String location;
    private BigDecimal price;
    private Integer durationMinutes;
    private List<String> tags;
    private String matchReason;
    private String imageUrl;

    public static RecommendedExperienceResponse from(Experience experience, String matchReason) {
        String imageUrl = experience.getImages() != null && !experience.getImages().isEmpty()
                ? experience.getImages().get(0).getImageUrl()
                : null;

        return RecommendedExperienceResponse.builder()
                .id(experience.getId())
                .title(experience.getTitle())
                .location(experience.getLocationAddress())
                .price(experience.getPrice())
                .durationMinutes(experience.getDurationMinutes())
                .tags(experience.getTags() == null ? List.of() : List.copyOf(experience.getTags()))
                .matchReason(matchReason)
                .imageUrl(imageUrl)
                .build();
    }
}
