package com.janginharou.domain.experience.dto;

import com.janginharou.domain.experience.entity.Experience;
import com.janginharou.domain.experience.entity.ExperienceDifficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceResponse {

    private Long id;
    private Long artisanId;
    private String title;
    private String description;
    private BigDecimal price;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private Set<String> supportedLanguages;
    private ExperienceDifficulty difficulty;
    private Boolean isActive;
    private String imageUrl;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ExperienceResponse from(Experience experience) {
        return ExperienceResponse.builder()
                .id(experience.getId())
                .artisanId(experience.getArtisan().getId())
                .title(experience.getTitle())
                .description(experience.getDescription())
                .price(experience.getPrice())
                .startDateTime(experience.getStartDateTime())
                .endDateTime(experience.getEndDateTime())
                .maxParticipants(experience.getMaxParticipants())
                .currentParticipants(experience.getCurrentParticipants())
                .supportedLanguages(experience.getSupportedLanguages())
                .difficulty(experience.getDifficulty())
                .isActive(experience.getIsActive())
                .imageUrl(experience.getImageUrl())
                .location(experience.getLocation())
                .createdAt(experience.getCreatedAt())
                .updatedAt(experience.getUpdatedAt())
                .build();
    }
}
