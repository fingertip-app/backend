package com.janginharou.domain.banner.controller;

import com.janginharou.domain.banner.dto.BannerResponse;
import com.janginharou.domain.banner.service.BannerService;
import com.janginharou.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    /**
     * 히어로 배너 목록 조회
     * GET /api/banners/hero
     */
    @GetMapping("/hero")
    public ApiResponse<List<BannerResponse>> getHeroBanners() {
        List<BannerResponse> banners = bannerService.getHeroBanners().stream()
            .map(BannerResponse::from)
            .toList();
        return ApiResponse.ok(banners);
    }

    /**
     * 활성화된 모든 배너 조회
     * GET /api/banners
     */
    @GetMapping
    public ApiResponse<List<BannerResponse>> getAllActiveBanners() {
        List<BannerResponse> banners = bannerService.getAllActiveBanners().stream()
            .map(BannerResponse::from)
            .toList();
        return ApiResponse.ok(banners);
    }
}
