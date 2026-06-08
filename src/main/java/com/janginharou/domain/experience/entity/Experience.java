package com.janginharou.domain.experience.entity;

import com.janginharou.domain.artisan.entity.Artisan;
import com.janginharou.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "experiences")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Experience extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artisan_id", nullable = false)
    private Artisan artisan;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String culturalStory;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    private Integer maxParticipants;

    @Column(length = 20)
    private String difficulty;

    @ElementCollection
    @CollectionTable(name = "experience_supported_languages", joinColumns = @JoinColumn(name = "experience_id"))
    @Column(name = "language")
    private List<String> supportedLanguages;

    @Column(columnDefinition = "TEXT")
    private String locationAddress;

    @Column(precision = 10, scale = 7)
    private BigDecimal locationLat;

    @Column(precision = 10, scale = 7)
    private BigDecimal locationLng;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @ElementCollection
    @CollectionTable(name = "experience_tags", joinColumns = @JoinColumn(name = "experience_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();
}