package com.janginharou.domain.banner.dto;

import com.janginharou.domain.banner.entity.Banner;

import java.time.LocalDateTime;

public record BannerResponse(
    Long id,
    String title,
    String subtitle,
    String tag,
    String imageUrl,
    String bannerType,
    Integer displayOrder,
    LocalDateTime createdAt
) {
    public static BannerResponse from(Banner banner) {
        return new BannerResponse(
            banner.getId(),
            banner.getTitle(),
            banner.getSubtitle(),
            banner.getTag(),
            banner.getImageUrl(),
            banner.getBannerType(),
            banner.getDisplayOrder(),
            banner.getCreatedAt()
        );
    }
}
