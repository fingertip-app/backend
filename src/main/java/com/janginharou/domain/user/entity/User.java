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
    private String provider;  // kakao, google, apple, supabase

    @Column(name = "provider_id", unique = true, length = 255)
    private String providerId;

    @Column(length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.USER;

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

    public static User supabaseUser(String supabaseId, String email, String nickname, String name, String phone) {
        return User.builder()
                .email(email)
                .provider("supabase")
                .providerId(supabaseId)
                .nickname(nickname)
                .name(name)
                .phone(phone)
                .role(UserRole.USER)
                .isActive(true)
                .build();
    }

    public void updateProfile(String name, String nickname, String phone, String profileImageUrl, List<String> preferredCategories) {
        if (name != null) {
            this.name = name;
        }
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (phone != null) {
            this.phone = phone;
        }
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
        if (preferredCategories != null) {
            this.preferredCategories = preferredCategories;
        }
    }

    public void changeRole(UserRole role) {
        this.role = role;
    }

    public void deactivate() {
        this.isActive = false;
    }
}
