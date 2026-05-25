package com.janginharou.domain.cardnews.entity;

import com.janginharou.domain.experience.entity.Experience;
import com.janginharou.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "card_news")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardNews extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String kContentType;

    @ManyToOne
    @JoinColumn(name = "experience_id")
    private Experience linkedExperience;

    private String imageUrl;

    @ElementCollection
    @CollectionTable(name = "card_news_tags", joinColumns = @JoinColumn(name = "card_news_id"))
    @Column(name = "tag")
    private java.util.Set<String> personalizationTags;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private Integer viewCount;

    public void increaseViewCount() {
        this.viewCount++;
    }
}
