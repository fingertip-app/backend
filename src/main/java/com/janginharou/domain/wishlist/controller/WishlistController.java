package com.janginharou.domain.wishlist.controller;

import com.janginharou.domain.wishlist.dto.WishlistResponse;
import com.janginharou.domain.wishlist.service.WishlistService;
import com.janginharou.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wishlists")
@RequiredArgsConstructor
@Tag(name = "Wishlist API", description = "위시리스트 관련 API")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @Operation(summary = "내 위시리스트 조회", description = "현재 로그인한 사용자의 위시리스트 조회")
    public ResponseEntity<ApiResponse<List<WishlistResponse>>> getMyWishlists(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        List<WishlistResponse> wishlists = wishlistService.getUserWishlists(userId);
        return ResponseEntity.ok(ApiResponse.ok(wishlists));
    }

    @PostMapping("/{experienceId}")
    @Operation(summary = "위시리스트 추가", description = "체험을 위시리스트에 추가")
    public ResponseEntity<ApiResponse<WishlistResponse>> addToWishlist(
            @PathVariable Long experienceId,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        WishlistResponse response = wishlistService.addToWishlist(userId, experienceId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @DeleteMapping("/{experienceId}")
    @Operation(summary = "위시리스트 제거", description = "체험을 위시리스트에서 제거")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
            @PathVariable Long experienceId,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        wishlistService.removeFromWishlist(userId, experienceId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/check/{experienceId}")
    @Operation(summary = "위시리스트 존재 여부 확인", description = "특정 체험이 위시리스트에 있는지 확인")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkWishlist(
            @PathVariable Long experienceId,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        boolean isInWishlist = wishlistService.isInWishlist(userId, experienceId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("isInWishlist", isInWishlist)));
    }
}
