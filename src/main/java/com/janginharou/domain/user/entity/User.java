package com.janginharou.domain.user.entity;

import com.janginharou.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 50)
    private String provider;  // kakao, google, apple

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(columnDefinition = "TEXT")
    private String profileImageUrl;

    @ElementCollection
    @CollectionTable(name = "user_preferred_categories", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "category")
    private List<String> preferredCategories;

    @ElementCollection
    @CollectionTable(name = "user_preferred_content_types", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "content_type")
    private List<String> preferredContentTypes;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
