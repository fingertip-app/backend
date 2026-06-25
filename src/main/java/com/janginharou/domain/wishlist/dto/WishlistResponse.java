package com.janginharou.domain.wishlist.dto;

import com.janginharou.domain.experience.entity.Experience;
import com.janginharou.domain.wishlist.entity.Wishlist;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WishlistResponse(
        Long id,
        Long experienceId,
        String experienceTitle,
        String experienceCategory,
        String experienceLocation,
        BigDecimal experiencePrice,
        Integer experienceDurationMinutes,
        String experienceImageUrl,
        LocalDateTime createdAt
) {
    public static WishlistResponse from(Wishlist wishlist) {
        Experience experience = wishlist.getExperience();

        // 첫 번째 이미지 URL 추출 (없으면 null)
        String imageUrl = null;
        if (experience.getImages() != null && !experience.getImages().isEmpty()) {
            imageUrl = experience.getImages().get(0).getImageUrl();
        }

        return new WishlistResponse(
                wishlist.getId(),
                experience.getId(),
                experience.getTitle(),
                experience.getCategory(),
                experience.getLocationAddress(),
                experience.getPrice(),
                experience.getDurationMinutes(),
                imageUrl,
                wishlist.getCreatedAt()
        );
    }
}
