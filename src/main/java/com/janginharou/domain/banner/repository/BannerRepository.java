package com.janginharou.domain.banner.repository;

import com.janginharou.domain.banner.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {

    /**
     * 히어로 배너 조회 (활성화 + 기간 내 + 정렬순)
     */
    @Query("""
        SELECT b FROM Banner b
        WHERE b.isActive = true
        AND b.bannerType = 'HERO'
        AND (b.startDate IS NULL OR b.startDate <= :now)
        AND (b.endDate IS NULL OR b.endDate >= :now)
        ORDER BY b.displayOrder ASC, b.createdAt DESC
        """)
    List<Banner> findActiveHeroBanners(LocalDateTime now);

    /**
     * 활성화된 모든 배너 조회
     */
    List<Banner> findByIsActiveTrueOrderByDisplayOrderAscCreatedAtDesc();
}
