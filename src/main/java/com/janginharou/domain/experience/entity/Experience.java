package com.janginharou.domain.experience.entity;

import com.janginharou.domain.artisan.entity.Artisan;
import com.janginharou.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @ManyToOne
    @JoinColumn(name = "artisan_id", nullable = false)
    private Artisan artisan;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Column(nullable = false)
    private Integer maxParticipants;

    private Integer currentParticipants;

    @ElementCollection
    @CollectionTable(name = "experience_languages", joinColumns = @JoinColumn(name = "experience_id"))
    @Column(name = "language")
    private java.util.Set<String> supportedLanguages;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExperienceDifficulty difficulty;

    @Column(nullable = false)
    private Boolean isActive;

    private String imageUrl;
    private String location;
}