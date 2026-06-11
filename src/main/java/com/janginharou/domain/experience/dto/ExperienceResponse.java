package com.janginharou.domain.experience.dto;

import com.janginharou.domain.experience.entity.Experience;
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
public class ExperienceResponse {

    private Long id;
    private Long artisanId;
    private String title;
    private String description;
    private String culturalStory;
    private String category;
    private BigDecimal price;
    private Integer durationMinutes;
    private Integer maxParticipants;
    private String difficulty;
    private List<String> supportedLanguages;
    private String locationAddress;
    private BigDecimal locationLat;
    private BigDecimal locationLng;
    private Boolean isActive;
    private List<ScheduleResponse> schedules;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ExperienceResponse from(Experience experience) {
        return ExperienceResponse.builder()
                .id(experience.getId())
                .artisanId(experience.getArtisan().getId())
                .title(experience.getTitle())
                .description(experience.getDescription())
                .culturalStory(experience.getCulturalStory())
                .category(experience.getCategory())
                .price(experience.getPrice())
                .durationMinutes(experience.getDurationMinutes())
                .maxParticipants(experience.getMaxParticipants())
                .difficulty(experience.getDifficulty())
                .supportedLanguages(experience.getSupportedLanguages())
                .locationAddress(experience.getLocationAddress())
                .locationLat(experience.getLocationLat())
                .locationLng(experience.getLocationLng())
                .isActive(experience.getIsActive())
                .createdAt(experience.getCreatedAt())
                .updatedAt(experience.getUpdatedAt())
                .build();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduleResponse {
        private Long id;
        private LocalDateTime scheduledAt;
        private Integer availableSlots;
        private Integer bookedSlots;
        private Integer remainingSlots;
        private Boolean isActive;
    }
}
