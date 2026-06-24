package com.janginharou.domain.user.controller;

import com.janginharou.domain.user.dto.UserRequest;
import com.janginharou.domain.user.dto.UserResponse;
import com.janginharou.domain.user.dto.UserStatsResponse;
import com.janginharou.domain.user.service.UserService;
import com.janginharou.global.common.ApiResponse;
import com.janginharou.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User API", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;

    @GetMapping("/check/email")
    @Operation(summary = "이메일 중복 확인", description = "이메일 사용 가능 여부 확인")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestParam String email) {
        boolean available = !userService.existsByEmail(email);
        return ResponseEntity.ok(ApiResponse.ok(available));
    }

    @GetMapping("/check/nickname")
    @Operation(summary = "닉네임 중복 확인", description = "닉네임 사용 가능 여부 확인")
    public ResponseEntity<ApiResponse<Boolean>> checkNickname(@RequestParam String nickname) {
        boolean available = !userService.existsByNickname(nickname);
        return ResponseEntity.ok(ApiResponse.ok(available));
    }

    @GetMapping("/me")
    @Operation(summary = "내 프로필 조회", description = "인증된 사용자 프로필 조회")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        UserResponse response = UserResponse.from(userService.getUserById(currentUser.id()));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/me")
    @Operation(summary = "내 프로필 수정", description = "인증된 사용자 프로필 수정")
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestBody UserRequest request
    ) {
        UserResponse response = UserResponse.from(userService.updateProfile(
                currentUser.id(),
                request.getName(),
                request.getNickname(),
                request.getPhone(),
                request.getProfileImageUrl(),
                request.getPreferredCategories()
        ));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/me/stats")
    @Operation(summary = "내 활동 통계 조회", description = "찜한 체험 수, 작성한 후기 수 등 마이페이지 통계 조회")
    public ResponseEntity<ApiResponse<UserStatsResponse>> getMyStats(
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        UserStatsResponse response = userService.getUserStats(currentUser.id());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "사용자 조회", description = "사용자 ID로 사용자 정보 조회")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long userId) {
        UserResponse response = UserResponse.from(userService.getUserById(userId));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    @Operation(summary = "사용자 생성", description = "새로운 사용자 생성")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody UserRequest request) {
        // TODO: 소셜 로그인 토큰 검증 후 사용자 저장
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(null, "User created successfully"));
    }

    @PutMapping("/{userId}")
    @Operation(summary = "사용자 수정", description = "사용자 정보 수정")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long userId,
            @RequestBody UserRequest request) {
        // TODO: 사용자 정보 수정 처리
        return ResponseEntity.ok(ApiResponse.ok(null, "User updated successfully"));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "사용자 삭제", description = "사용자 삭제 (탈퇴)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        // TODO: 사용자 탈퇴 처리
        return ResponseEntity.ok(ApiResponse.ok(null, "User deleted successfully"));
    }
}
