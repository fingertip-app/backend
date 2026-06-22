package com.janginharou.domain.experience.dto;
import com.janginharou.domain.experience.entity.ExperienceImage;
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
    private List<ImageResponse> images;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
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
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImageResponse {
        private Long id;
        private String imageUrl;
        private Integer displayOrder;

        public static ImageResponse from(ExperienceImage image) {
            return ImageResponse.builder()
                    .id(image.getId())
                    .imageUrl(image.getImageUrl())
                    .displayOrder(image.getDisplayOrder())
                    .build();
        }
    }
}
