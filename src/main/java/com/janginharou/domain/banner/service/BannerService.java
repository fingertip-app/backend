package com.janginharou.domain.banner.service;

import com.janginharou.domain.banner.entity.Banner;
import com.janginharou.domain.banner.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerService {

    private final BannerRepository bannerRepository;

    /**
     * 히어로 배너 목록 조회
     */
    public List<Banner> getHeroBanners() {
        return bannerRepository.findActiveHeroBanners(LocalDateTime.now());
    }

    /**
     * 활성화된 모든 배너 조회
     */
    public List<Banner> getAllActiveBanners() {
        return bannerRepository.findByIsActiveTrueOrderByDisplayOrderAscCreatedAtDesc();
    }
}
