package com.janginharou.domain.artisan.dto;

import com.janginharou.domain.artisan.entity.Artisan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtisanResponse {

    private Long id;
    private Long userId;
    private String name;
    private String heritageCategory;
    private String certificationNumber;
    private String bio;
    private String profileImageUrl;
    private String introVideoUrl;
    private Boolean isVerified;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ArtisanResponse from(Artisan artisan) {
        return ArtisanResponse.builder()
                .id(artisan.getId())
                .userId(artisan.getUser().getId())
                .name(artisan.getName())
                .heritageCategory(artisan.getHeritageCategory())
                .certificationNumber(artisan.getCertificationNumber())
                .bio(artisan.getBio())
                .profileImageUrl(artisan.getProfileImageUrl())
                .introVideoUrl(artisan.getIntroVideoUrl())
                .isVerified(artisan.getIsVerified())
                .isActive(artisan.getIsActive())
                .createdAt(artisan.getCreatedAt())
                .updatedAt(artisan.getUpdatedAt())
                .build();
    }
}
