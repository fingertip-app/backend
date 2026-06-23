package com.janginharou.domain.user.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    @Email
    private String email;

    private String nickname;

    private String profileImageUrl;
    private List<String> preferredCategories;
}
