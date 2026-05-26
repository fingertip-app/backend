package com.janginharou.domain.cardnews.dto;

import com.janginharou.domain.cardnews.entity.CardNews;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardNewsResponse {

    private Long id;
    private String title;
    private String contentType;
    private String imageUrl;
    private String aiExplanation;
    private List<String> categoryTags;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static CardNewsResponse from(CardNews cardNews) {
        return CardNewsResponse.builder()
                .id(cardNews.getId())
                .title(cardNews.getTitle())
                .contentType(cardNews.getContentType())
                .imageUrl(cardNews.getImageUrl())
                .aiExplanation(cardNews.getAiExplanation())
                .categoryTags(cardNews.getCategoryTags())
                .isActive(cardNews.getIsActive())
                .createdAt(cardNews.getCreatedAt())
                .build();
    }
}
