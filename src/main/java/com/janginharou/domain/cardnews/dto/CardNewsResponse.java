package com.janginharou.domain.cardnews.dto;

import com.janginharou.domain.cardnews.entity.CardNews;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardNewsResponse {

    private Long id;
    private String title;
    private String description;
    private String kContentType;
    private Long linkedExperienceId;
    private String imageUrl;
    private Set<String> personalizationTags;
    private Boolean isActive;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CardNewsResponse from(CardNews cardNews) {
        return CardNewsResponse.builder()
                .id(cardNews.getId())
                .title(cardNews.getTitle())
                .description(cardNews.getDescription())
                .kContentType(cardNews.getKContentType())
                .linkedExperienceId(cardNews.getLinkedExperience() != null ? cardNews.getLinkedExperience().getId() : null)
                .imageUrl(cardNews.getImageUrl())
                .personalizationTags(cardNews.getPersonalizationTags())
                .isActive(cardNews.getIsActive())
                .viewCount(cardNews.getViewCount())
                .createdAt(cardNews.getCreatedAt())
                .updatedAt(cardNews.getUpdatedAt())
                .build();
    }
}
