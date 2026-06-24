package com.janginharou.domain.artisan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtisanStatsResponse {

    private Long pendingReservationCount;
    private Long activeExperienceCount;
    private Long monthlyRevenue;

    public static ArtisanStatsResponse of(Long pendingReservationCount, Long activeExperienceCount, Long monthlyRevenue) {
        return ArtisanStatsResponse.builder()
                .pendingReservationCount(pendingReservationCount)
                .activeExperienceCount(activeExperienceCount)
                .monthlyRevenue(monthlyRevenue)
                .build();
    }
}
