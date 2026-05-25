package com.janginharou.domain.cardnews.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardNewsRequest {

    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String kContentType;

    private Long linkedExperienceId;
    private String imageUrl;
    private Set<String> personalizationTags;
}
