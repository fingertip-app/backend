package com.janginharou.domain.user.controller;

import com.janginharou.domain.user.dto.UserResponse;
import com.janginharou.domain.user.service.UserService;
import com.janginharou.global.common.ApiResponse;
import com.janginharou.global.exception.UnauthorizedException;
import com.janginharou.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "Supabase 인증 연동 API")
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "Supabase JWT 로그인", description = "Authorization 헤더의 Supabase JWT를 검증하고 사용자 프로필을 반환")
    public ResponseEntity<ApiResponse<UserResponse>> login(
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        if (currentUser == null) {
            throw new UnauthorizedException("Supabase access token is required");
        }
        UserResponse response = UserResponse.from(userService.getUserById(currentUser.id()));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
