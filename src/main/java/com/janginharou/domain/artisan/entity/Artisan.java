package com.janginharou.domain.artisan.entity;

import com.janginharou.domain.user.entity.User;
import com.janginharou.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "artisans")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Artisan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String heritageCategory;

    @Column(unique = true, length = 100)
    private String certificationNumber;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(columnDefinition = "TEXT")
    private String profileImageUrl;

    @Column(columnDefinition = "TEXT")
    private String introVideoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ArtisanVerificationStatus certificationStatus = ArtisanVerificationStatus.PENDING;

    private LocalDateTime verifiedAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    private String address;

    private Double latitude;

    private Double longitude;

    public void approve() {
        this.certificationStatus = ArtisanVerificationStatus.APPROVED;
        this.isVerified = true;
        this.verifiedAt = LocalDateTime.now();
    }

    public void reject() {
        this.certificationStatus = ArtisanVerificationStatus.REJECTED;
        this.isVerified = false;
        this.verifiedAt = null;
    }
}
