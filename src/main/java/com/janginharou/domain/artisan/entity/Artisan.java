package com.janginharou.domain.artisan.entity;

import com.janginharou.domain.user.entity.User;
import com.janginharou.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String intangibleHeritageName;

    @Column(nullable = false)
    private String intangibleHeritageType;

    private String certificationImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArtisanVerificationStatus verificationStatus;

    private String bio;
    private String profileImageUrl;
    private Integer yearsOfExperience;

    @Column(nullable = false)
    private Boolean isActive;
}
