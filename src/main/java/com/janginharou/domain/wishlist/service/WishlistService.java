package com.janginharou.domain.wishlist.service;

import com.janginharou.domain.experience.entity.Experience;
import com.janginharou.domain.experience.repository.ExperienceRepository;
import com.janginharou.domain.user.entity.User;
import com.janginharou.domain.user.repository.UserRepository;
import com.janginharou.domain.wishlist.dto.WishlistResponse;
import com.janginharou.domain.wishlist.entity.Wishlist;
import com.janginharou.domain.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ExperienceRepository experienceRepository;

    /**
     * 사용자의 위시리스트 조회
     */
    public List<WishlistResponse> getUserWishlists(Long userId) {
        return wishlistRepository.findByUserId(userId).stream()
                .map(WishlistResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 위시리스트 추가
     */
    @Transactional
    public WishlistResponse addToWishlist(Long userId, Long experienceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new IllegalArgumentException("체험을 찾을 수 없습니다: " + experienceId));

        // 이미 존재하는지 확인
        if (wishlistRepository.existsByUserIdAndExperienceId(userId, experienceId)) {
            throw new IllegalStateException("이미 위시리스트에 추가된 체험입니다.");
        }

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .experience(experience)
                .build();

        Wishlist saved = wishlistRepository.save(wishlist);
        log.info("위시리스트 추가: userId={}, experienceId={}", userId, experienceId);

        return WishlistResponse.from(saved);
    }

    /**
     * 위시리스트 제거
     */
    @Transactional
    public void removeFromWishlist(Long userId, Long experienceId) {
        Wishlist wishlist = wishlistRepository.findByUserIdAndExperienceId(userId, experienceId)
                .orElseThrow(() -> new IllegalArgumentException("위시리스트에 해당 체험이 없습니다."));

        wishlistRepository.delete(wishlist);
        log.info("위시리스트 제거: userId={}, experienceId={}", userId, experienceId);
    }

    /**
     * 위시리스트 존재 여부 확인
     */
    public boolean isInWishlist(Long userId, Long experienceId) {
        return wishlistRepository.existsByUserIdAndExperienceId(userId, experienceId);
    }
}
