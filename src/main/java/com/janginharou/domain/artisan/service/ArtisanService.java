package com.janginharou.domain.artisan.service;

import com.janginharou.domain.artisan.entity.Artisan;
import com.janginharou.domain.artisan.entity.ArtisanVerificationStatus;
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
    public List<Artisan> getApprovedArtisans() {
        return artisanRepository.findByVerificationStatus(ArtisanVerificationStatus.APPROVED);
    }

    @Transactional
    public Artisan createArtisan(Artisan artisan) {
        // TODO: 장인 가입 신청 처리 (인증서 S3 업로드, 초기 상태 PENDING으로 설정)
        return artisanRepository.save(artisan);
    }

    @Transactional
    public Artisan approveArtisan(Long artisanId) {
        // TODO: 장인 인증 승인 처리 (관리자용)
        Artisan artisan = getArtisanById(artisanId);
        return artisan;
    }

    @Transactional
    public Artisan rejectArtisan(Long artisanId, String rejectionReason) {
        // TODO: 장인 인증 거절 처리 (거절 사유 저장)
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
