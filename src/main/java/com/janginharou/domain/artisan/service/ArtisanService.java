package com.janginharou.domain.artisan.service;

import com.janginharou.domain.artisan.entity.Artisan;
import com.janginharou.domain.artisan.repository.ArtisanRepository;
import com.janginharou.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtisanService {

    private final ArtisanRepository artisanRepository;

    @Transactional(readOnly = true)
    public Artisan getArtisanById(Long artisanId) {
        return artisanRepository.findById(artisanId)
                .orElseThrow(() -> new ResourceNotFoundException("Artisan", "id", artisanId));
    }

    @Transactional(readOnly = true)
    public Artisan getArtisanByUserId(Long userId) {
        return artisanRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Artisan", "userId", userId));
    }

    @Transactional(readOnly = true)
    public List<Artisan> getVerifiedArtisans() {
        return artisanRepository.findByIsVerifiedTrue();
    }

    @Transactional
    public Artisan createArtisan(Artisan artisan) {
        // TODO: 장인 가입 신청 처리
        return artisanRepository.save(artisan);
    }

    @Transactional
    public Artisan verifyArtisan(Long artisanId) {
        // TODO: 장인 인증 승인 처리 (관리자용)
        Artisan artisan = getArtisanById(artisanId);
        return artisan;
    }

    @Transactional
    public Artisan updateArtisan(Long artisanId, Artisan updateData) {
        // TODO: 장인 정보 수정 처리
        Artisan artisan = getArtisanById(artisanId);
        return artisan;
    }
}
