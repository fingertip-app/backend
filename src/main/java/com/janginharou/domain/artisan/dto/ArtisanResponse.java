package com.janginharou.domain.artisan.dto;

import com.janginharou.domain.artisan.entity.Artisan;
import com.janginharou.domain.artisan.entity.ArtisanVerificationStatus;
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
    private String intangibleHeritageName;
    private String intangibleHeritageType;
    private String certificationImageUrl;
    private ArtisanVerificationStatus verificationStatus;
    private String bio;
    private String profileImageUrl;
    private Integer yearsOfExperience;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ArtisanResponse from(Artisan artisan) {
        return ArtisanResponse.builder()
                .id(artisan.getId())
                .userId(artisan.getUser().getId())
                .intangibleHeritageName(artisan.getIntangibleHeritageName())
                .intangibleHeritageType(artisan.getIntangibleHeritageType())
                .certificationImageUrl(artisan.getCertificationImageUrl())
                .verificationStatus(artisan.getVerificationStatus())
                .bio(artisan.getBio())
                .profileImageUrl(artisan.getProfileImageUrl())
                .yearsOfExperience(artisan.getYearsOfExperience())
                .isActive(artisan.getIsActive())
                .createdAt(artisan.getCreatedAt())
                .updatedAt(artisan.getUpdatedAt())
                .build();
    }
}
