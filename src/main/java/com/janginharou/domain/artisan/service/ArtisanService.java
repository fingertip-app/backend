package com.janginharou.domain.artisan.service;

import com.janginharou.domain.artisan.dto.ArtisanStatsResponse;
import com.janginharou.domain.artisan.entity.Artisan;
import com.janginharou.domain.artisan.dto.ArtisanRequest;
import com.janginharou.domain.artisan.entity.ArtisanVerificationStatus;
import com.janginharou.domain.artisan.repository.ArtisanRepository;
import com.janginharou.domain.experience.repository.ExperienceRepository;
import com.janginharou.domain.reservation.entity.ReservationStatus;
import com.janginharou.domain.reservation.repository.ReservationRepository;
import com.janginharou.domain.user.entity.User;
import com.janginharou.domain.user.entity.UserRole;
import com.janginharou.domain.user.repository.UserRepository;
import com.janginharou.global.exception.InvalidRequestException;
import com.janginharou.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtisanService {

    private final ArtisanRepository artisanRepository;
    private final UserRepository userRepository;
    private final ExperienceRepository experienceRepository;
    private final ReservationRepository reservationRepository;

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

    @Transactional(readOnly = true)
    public List<Artisan> getArtisansByStatus(ArtisanVerificationStatus status) {
        if (status == null) {
            return artisanRepository.findAll();
        }
        return artisanRepository.findByCertificationStatus(status);
    }

    @Transactional
    public Artisan apply(Long userId, ArtisanRequest request) {
        if (artisanRepository.existsByUserId(userId)) {
            throw new InvalidRequestException("Artisan application already exists");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Artisan artisan = Artisan.builder()
                .user(user)
                .name(request.getName())
                .heritageCategory(request.getHeritageCategory())
                .certificationNumber(request.getCertificationNumber())
                .bio(request.getBio())
                .profileImageUrl(request.getProfileImageUrl())
                .introVideoUrl(request.getIntroVideoUrl())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .certificationStatus(ArtisanVerificationStatus.PENDING)
                .isVerified(false)
                .isActive(true)
                .build();
        return artisanRepository.save(artisan);
    }

    @Transactional
    public Artisan approveArtisan(Long artisanId) {
        Artisan artisan = getArtisanById(artisanId);
        artisan.approve();
        artisan.getUser().changeRole(UserRole.ARTISAN);
        return artisan;
    }

    @Transactional
    public Artisan rejectArtisan(Long artisanId) {
        Artisan artisan = getArtisanById(artisanId);
        artisan.reject();
        return artisan;
    }

    @Transactional
    public Artisan createArtisan(Artisan artisan) {
        return artisanRepository.save(artisan);
    }

    @Transactional
    public Artisan updateArtisan(Long artisanId, Artisan updateData) {
        // TODO: 장인 정보 수정 처리
        Artisan artisan = getArtisanById(artisanId);
        return artisan;
    }

    @Transactional(readOnly = true)
    public ArtisanStatsResponse getArtisanStats(Long artisanId) {
        // 장인 존재 여부 확인
        Artisan artisan = getArtisanById(artisanId);

        // 신규 예약 개수 (PENDING 상태)
        Long pendingReservationCount = experienceRepository.findByArtisanId(artisanId).stream()
                .flatMap(experience -> reservationRepository.findByExperienceIdAndStatus(
                        experience.getId(),
                        ReservationStatus.PENDING
                ).stream())
                .count();

        // 운영 중인 클래스 개수 (활성화된 체험)
        Long activeExperienceCount = (long) experienceRepository.findByArtisanIdAndIsActiveTrue(artisanId).size();

        // 이달의 수익 (체험 완료 건, COMPLETED 상태의 totalPrice 합산)
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

        Long monthlyRevenue = experienceRepository.findByArtisanId(artisanId).stream()
                .flatMap(experience -> reservationRepository.findByExperienceIdAndStatus(
                        experience.getId(),
                        ReservationStatus.COMPLETED
                ).stream())
                .filter(reservation -> {
                    LocalDateTime completedAt = reservation.getUpdatedAt();
                    return completedAt != null &&
                           completedAt.isAfter(startOfMonth) &&
                           completedAt.isBefore(endOfMonth);
                })
                .map(reservation -> reservation.getTotalPrice() != null ? reservation.getTotalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .longValue();

        return ArtisanStatsResponse.of(pendingReservationCount, activeExperienceCount, monthlyRevenue);
    }
}
