package com.janginharou.domain.experience.dto;

import com.janginharou.domain.experience.entity.ExperienceDifficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceRequest {

    @NotBlank
    private String title;

    private String description;
    private String culturalStory;
    private String category;

    @NotNull
    @Positive
    private BigDecimal price;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    private Integer durationMinutes;

    @NotNull
    @Positive
    private Integer maxParticipants;

    private Set<String> supportedLanguages;

    @NotNull
    private ExperienceDifficulty difficulty;

    private String imageUrl;
    private String location;
    private String locationAddress;
    private BigDecimal locationLat;
    private BigDecimal locationLng;
    private List<String> tags;
    private List<ScheduleRequest> schedules;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduleRequest {

        @NotNull
        private LocalDateTime scheduledAt;

        @NotNull
        @Positive
        private Integer availableSlots;
    }
}
