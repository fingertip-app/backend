package com.janginharou.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String name;

    private String phoneNumber;
    private String profileImageUrl;
}
