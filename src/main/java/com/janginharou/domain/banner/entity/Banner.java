package com.janginharou.domain.banner.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "banners")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 500)
    private String subtitle;

    @Column(nullable = false, length = 50)
    private String tag;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    @Column(length = 50)
    private String bannerType;  // HERO, PROMOTION, EVENT

    @Column
    private Integer displayOrder;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column
    private LocalDateTime startDate;

    @Column
    private LocalDateTime endDate;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
