package com.janginharou.domain.artisan.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtisanRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String heritageCategory;

    private String certificationNumber;
    private String bio;
    private String profileImageUrl;
    private String introVideoUrl;
}
