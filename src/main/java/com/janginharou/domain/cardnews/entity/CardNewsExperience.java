package com.janginharou.domain.cardnews.entity;

import com.janginharou.domain.experience.entity.Experience;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "card_news_experiences",
        uniqueConstraints = @UniqueConstraint(columnNames = {"card_news_id", "experience_id"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardNewsExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_news_id", nullable = false)
    private CardNews cardNews;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experience_id", nullable = false)
    private Experience experience;

    @Column(precision = 4, scale = 3)
    private BigDecimal similarityScore;
}
