package com.janginharou.domain.user.dto;

import com.janginharou.domain.user.entity.SocialProvider;
import com.janginharou.domain.user.entity.User;
import com.janginharou.domain.user.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String name;
    private String profileImageUrl;
    private String phoneNumber;
    private UserRole role;
    private SocialProvider socialProvider;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .profileImageUrl(user.getProfileImageUrl())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .socialProvider(user.getSocialProvider())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
