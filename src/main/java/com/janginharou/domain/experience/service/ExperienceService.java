package com.janginharou.domain.experience.service;

import com.janginharou.domain.artisan.entity.Artisan;
import com.janginharou.domain.artisan.repository.ArtisanRepository;
import com.janginharou.domain.experience.dto.ExperienceRequest;
import com.janginharou.domain.experience.dto.ExperienceResponse;
import com.janginharou.domain.experience.entity.Experience;
import com.janginharou.domain.experience.entity.ExperienceImage;
import com.janginharou.domain.experience.entity.ExperienceSchedule;
import com.janginharou.domain.experience.repository.ExperienceRepository;
import com.janginharou.domain.experience.repository.ExperienceScheduleRepository;
import com.janginharou.domain.reservation.entity.ReservationStatus;
import com.janginharou.domain.reservation.repository.ReservationRepository;
import com.janginharou.global.exception.InvalidRequestException;
import com.janginharou.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final ExperienceScheduleRepository experienceScheduleRepository;
    private final ReservationRepository reservationRepository;
    private final ArtisanRepository artisanRepository;
    private final com.janginharou.domain.experience.repository.ExperienceImageRepository experienceImageRepository;
    private final com.janginharou.domain.review.repository.ReviewRepository reviewRepository;

    private static final Set<ReservationStatus> CAPACITY_HOLDING_STATUSES = EnumSet.of(
            ReservationStatus.PENDING,
            ReservationStatus.APPROVED,
            ReservationStatus.PAID,
            ReservationStatus.CONFIRMED
    );

    @Transactional(readOnly = true)
    public Experience getExperienceById(Long experienceId) {
        return experienceRepository.findById(experienceId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience", "id", experienceId));
    }

    @Transactional(readOnly = true)
    public List<Experience> getExperiencesByArtisanId(Long artisanId) {
        return experienceRepository.findByArtisanId(artisanId);
    }

    @Transactional(readOnly = true)
    public List<ExperienceResponse> getExperienceResponsesByArtisanId(Long artisanId) {
        List<Experience> experiences = experienceRepository.findByArtisanId(artisanId);

        // 체험 ID 목록 추출
        List<Long> experienceIds = experiences.stream()
                .map(Experience::getId)
                .toList();

        // 리뷰 통계 한 번에 조회
        Map<Long, Map<String, Object>> reviewStatsMap = reviewRepository.getReviewStatsByExperienceIds(experienceIds)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        map -> ((Number) map.get("experienceId")).longValue(),
                        map -> map
                ));

        // DTO 변환
        return experiences.stream()
                .map(experience -> {
                    Map<String, Object> stats = reviewStatsMap.get(experience.getId());
                    Double avgRating = stats != null ? (Double) stats.get("avgRating") : 0.0;
                    Long reviewCount = stats != null ? ((Number) stats.get("reviewCount")).longValue() : 0L;
                    return toExperienceResponseWithSchedulesAndReviews(experience, avgRating, reviewCount);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Experience> getActiveExperiences() {
        return experienceRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<ExperienceResponse> getActiveExperienceResponses() {
        List<Experience> experiences = experienceRepository.findByIsActiveTrue();

        // 체험 ID 목록 추출
        List<Long> experienceIds = experiences.stream()
                .map(Experience::getId)
                .toList();

        // 리뷰 통계 한 번에 조회
        Map<Long, Map<String, Object>> reviewStatsMap = reviewRepository.getReviewStatsByExperienceIds(experienceIds)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        map -> ((Number) map.get("experienceId")).longValue(),
                        map -> map
                ));

        // DTO 변환
        return experiences.stream()
                .map(experience -> {
                    Map<String, Object> stats = reviewStatsMap.get(experience.getId());
                    Double avgRating = stats != null ? (Double) stats.get("avgRating") : 0.0;
                    Long reviewCount = stats != null ? ((Number) stats.get("reviewCount")).longValue() : 0L;
                    return toExperienceResponseWithSchedulesAndReviews(experience, avgRating, reviewCount);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public ExperienceResponse getExperienceDetail(Long experienceId) {
        Experience experience = getExperienceById(experienceId);
        List<ExperienceResponse.ScheduleResponse> schedules = experienceScheduleRepository
                .findByExperienceIdAndIsActiveTrue(experienceId)
                .stream()
                .map(this::toScheduleResponse)
                .toList();

        // 리뷰 통계 조회
        Double avgRating = reviewRepository.getAverageRating(experienceId);
        Long reviewCount = reviewRepository.getReviewCount(experienceId);

        return ExperienceResponse.builder()
                .id(experience.getId())
                .artisanId(experience.getArtisan().getId())
                .title(experience.getTitle())
                .description(experience.getDescription())
                .culturalStory(experience.getCulturalStory())
                .category(experience.getCategory())
                .price(experience.getPrice())
                .durationMinutes(experience.getDurationMinutes())
                .maxParticipants(experience.getMaxParticipants())
                .difficulty(experience.getDifficulty())
                .supportedLanguages(experience.getSupportedLanguages())
                .locationAddress(experience.getLocationAddress())
                .locationLat(experience.getLocationLat())
                .locationLng(experience.getLocationLng())
                .isActive(experience.getIsActive())
                .schedules(schedules)
                .images(toImageResponses(experience))
                .tags(toTagList(experience))
                .averageRating(avgRating != null ? avgRating : 0.0)
                .reviewCount(reviewCount != null ? reviewCount : 0L)
                .createdAt(experience.getCreatedAt())
                .updatedAt(experience.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ExperienceResponse> getUpcomingExperiences() {
        return getActiveExperienceResponses();
    }

    @Transactional
    public ExperienceResponse createExperience(Long artisanId, ExperienceRequest request) {
        Artisan artisan = artisanRepository.findById(artisanId)
                .orElseThrow(() -> new ResourceNotFoundException("Artisan", "id", artisanId));
        if (!Boolean.TRUE.equals(artisan.getIsVerified())) {
            throw new InvalidRequestException("Only verified artisans can create experiences");
        }

        List<ExperienceRequest.ScheduleRequest> schedules = resolveSchedules(request);
        Integer durationMinutes = resolveDurationMinutes(request);
        String category = hasText(request.getCategory()) ? request.getCategory() : artisan.getHeritageCategory();
        String locationAddress = hasText(request.getLocationAddress()) ? request.getLocationAddress() : request.getLocation();

        Experience experience = Experience.builder()
                .artisan(artisan)
                .title(request.getTitle())
                .description(request.getDescription())
                .culturalStory(request.getCulturalStory())
                .category(category)
                .price(request.getPrice())
                .durationMinutes(durationMinutes)
                .maxParticipants(request.getMaxParticipants())
                .difficulty(request.getDifficulty().name())
                .supportedLanguages(toList(request.getSupportedLanguages()))
                .locationAddress(locationAddress)
                .locationLat(request.getLocationLat())
                .locationLng(request.getLocationLng())
                .tags(resolveTags(request, category))
                .isActive(true)
                .build();

        Experience savedExperience = experienceRepository.save(experience);
        List<ExperienceSchedule> savedSchedules = schedules.stream()
                .map(schedule -> ExperienceSchedule.builder()
                        .experience(savedExperience)
                        .scheduledAt(schedule.getScheduledAt())
                        .availableSlots(schedule.getAvailableSlots())
                        .isActive(true)
                        .build())
                .map(experienceScheduleRepository::save)
                .toList();

        // 이미지 저장
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            ExperienceImage image = ExperienceImage.builder()
                    .experience(savedExperience)
                    .imageUrl(request.getImageUrl())
                    .displayOrder(0)
                    .build();
            experienceImageRepository.save(image);
        }

        return toExperienceResponse(savedExperience, savedSchedules);
    }

    @Transactional
    public ExperienceResponse updateExperience(Long experienceId, Long artisanId, ExperienceRequest request) {
        Experience experience = getExperienceById(experienceId);
        validateArtisanOwnsExperience(experience, artisanId);

        experience.update(
                request.getTitle(),
                request.getDescription(),
                request.getCulturalStory(),
                request.getCategory(),
                request.getPrice(),
                request.getDurationMinutes(),
                request.getMaxParticipants(),
                request.getDifficulty() != null ? request.getDifficulty().name() : null,
                toList(request.getSupportedLanguages()),
                request.getLocationAddress(),
                request.getLocationLat(),
                request.getLocationLng(),
                request.getTags()
        );

        // 이미지 업데이트 (기존 삭제 후 새로 추가)
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            experience.clearImages();
            ExperienceImage image = ExperienceImage.builder()
                    .experience(experience)
                    .imageUrl(request.getImageUrl())
                    .displayOrder(0)
                    .build();
            experienceImageRepository.save(image);
        }

        return toExperienceResponseWithSchedules(experience);
    }

    private void validateArtisanOwnsExperience(Experience experience, Long artisanId) {
        if (!experience.getArtisan().getId().equals(artisanId)) {
            throw new com.janginharou.global.exception.UnauthorizedException("You do not own this experience");
        }
    }

    @Transactional
    public void deleteExperience(Long experienceId, Long artisanId) {
        Experience experience = getExperienceById(experienceId);
        validateArtisanOwnsExperience(experience, artisanId);

        // 활성 예약이 있는지 확인
        boolean hasActiveReservations = reservationRepository.existsByExperienceIdAndStatusIn(
                experienceId,
                List.copyOf(CAPACITY_HOLDING_STATUSES)
        );

        if (hasActiveReservations) {
            throw new InvalidRequestException("Cannot delete experience with active reservations");
        }

        // Hard delete
        experienceRepository.delete(experience);
    }

    private ExperienceResponse.ScheduleResponse toScheduleResponse(ExperienceSchedule schedule) {
        Integer bookedSlots = reservationRepository.sumParticipantsByScheduleIdAndStatusIn(
                schedule.getId(),
                List.copyOf(CAPACITY_HOLDING_STATUSES)
        );
        int remainingSlots = Math.max(schedule.getAvailableSlots() - bookedSlots, 0);
        return ExperienceResponse.ScheduleResponse.builder()
                .id(schedule.getId())
                .scheduledAt(schedule.getScheduledAt())
                .availableSlots(schedule.getAvailableSlots())
                .bookedSlots(bookedSlots)
                .remainingSlots(remainingSlots)
                .isActive(schedule.getIsActive())
                .build();
    }

    private ExperienceResponse toExperienceResponseWithSchedules(Experience experience) {
        List<ExperienceSchedule> schedules = experienceScheduleRepository
                .findByExperienceIdAndIsActiveTrue(experience.getId());
        return toExperienceResponse(experience, schedules);
    }

    private ExperienceResponse toExperienceResponseWithSchedulesAndReviews(Experience experience, Double avgRating, Long reviewCount) {
        List<ExperienceSchedule> schedules = experienceScheduleRepository
                .findByExperienceIdAndIsActiveTrue(experience.getId());
        return toExperienceResponseWithReviews(experience, schedules, avgRating, reviewCount);
    }

    private ExperienceResponse toExperienceResponse(Experience experience, List<ExperienceSchedule> schedules) {
        return ExperienceResponse.builder()
                .id(experience.getId())
                .artisanId(experience.getArtisan().getId())
                .title(experience.getTitle())
                .description(experience.getDescription())
                .culturalStory(experience.getCulturalStory())
                .category(experience.getCategory())
                .price(experience.getPrice())
                .durationMinutes(experience.getDurationMinutes())
                .maxParticipants(experience.getMaxParticipants())
                .difficulty(experience.getDifficulty())
                .supportedLanguages(experience.getSupportedLanguages())
                .locationAddress(experience.getLocationAddress())
                .locationLat(experience.getLocationLat())
                .locationLng(experience.getLocationLng())
                .isActive(experience.getIsActive())
                .schedules(schedules.stream().map(this::toScheduleResponse).toList())
                .images(toImageResponses(experience))
                .tags(toTagList(experience))
                .averageRating(0.0)
                .reviewCount(0L)
                .createdAt(experience.getCreatedAt())
                .updatedAt(experience.getUpdatedAt())
                .build();
    }

    private ExperienceResponse toExperienceResponseWithReviews(Experience experience, List<ExperienceSchedule> schedules, Double avgRating, Long reviewCount) {
        return ExperienceResponse.builder()
                .id(experience.getId())
                .artisanId(experience.getArtisan().getId())
                .title(experience.getTitle())
                .description(experience.getDescription())
                .culturalStory(experience.getCulturalStory())
                .category(experience.getCategory())
                .price(experience.getPrice())
                .durationMinutes(experience.getDurationMinutes())
                .maxParticipants(experience.getMaxParticipants())
                .difficulty(experience.getDifficulty())
                .supportedLanguages(experience.getSupportedLanguages())
                .locationAddress(experience.getLocationAddress())
                .locationLat(experience.getLocationLat())
                .locationLng(experience.getLocationLng())
                .isActive(experience.getIsActive())
                .schedules(schedules.stream().map(this::toScheduleResponse).toList())
                .images(toImageResponses(experience))
                .tags(toTagList(experience))
                .averageRating(avgRating != null ? avgRating : 0.0)
                .reviewCount(reviewCount != null ? reviewCount : 0L)
                .createdAt(experience.getCreatedAt())
                .updatedAt(experience.getUpdatedAt())
                .build();
    }

    /**
     * Experience의 images 연관관계를 display_order 기준으로 정렬해 DTO 리스트로 변환한다.
     * images가 비어있거나 null이면 빈 리스트를 반환한다(프론트에서 null-safe하게 처리하도록).
     */
    private List<ExperienceResponse.ImageResponse> toImageResponses(Experience experience) {
        if (experience.getImages() == null) {
            return List.of();
        }
        return experience.getImages().stream()
                .sorted(Comparator.comparing(
                        ExperienceImage::getDisplayOrder,
                        Comparator.nullsFirst(Comparator.naturalOrder())
                ))
                .map(ExperienceResponse.ImageResponse::from)
                .toList();
    }

    /**
     * Experience의 tags를 반환한다. null이면 빈 리스트로 대체한다.
     */
    private List<String> toTagList(Experience experience) {
        return experience.getTags() != null ? experience.getTags() : List.of();
    }

    private List<ExperienceRequest.ScheduleRequest> resolveSchedules(ExperienceRequest request) {
        if (request.getSchedules() != null && !request.getSchedules().isEmpty()) {
            request.getSchedules().forEach(this::validateSchedule);
            return request.getSchedules();
        }
        if (request.getStartDateTime() == null) {
            throw new InvalidRequestException("At least one schedule is required");
        }
        return List.of(ExperienceRequest.ScheduleRequest.builder()
                .scheduledAt(request.getStartDateTime())
                .availableSlots(request.getMaxParticipants())
                .build());
    }

    private void validateSchedule(ExperienceRequest.ScheduleRequest schedule) {
        if (schedule.getScheduledAt() == null) {
            throw new InvalidRequestException("Schedule date time is required");
        }
        if (schedule.getAvailableSlots() == null || schedule.getAvailableSlots() <= 0) {
            throw new InvalidRequestException("Schedule available slots must be positive");
        }
    }

    private Integer resolveDurationMinutes(ExperienceRequest request) {
        if (request.getDurationMinutes() != null && request.getDurationMinutes() > 0) {
            return request.getDurationMinutes();
        }
        if (request.getStartDateTime() != null && request.getEndDateTime() != null) {
            long minutes = Duration.between(request.getStartDateTime(), request.getEndDateTime()).toMinutes();
            if (minutes > 0) {
                return Math.toIntExact(minutes);
            }
        }
        throw new InvalidRequestException("Duration minutes is required");
    }

    private List<String> resolveTags(ExperienceRequest request, String category) {
        List<String> tags = new ArrayList<>();
        if (hasText(category)) {
            tags.add(category);
        }
        if (request.getTags() != null) {
            request.getTags().stream()
                    .filter(this::hasText)
                    .map(String::trim)
                    .filter(tag -> !tags.contains(tag))
                    .forEach(tags::add);
        }
        return tags;
    }

    private List<String> toList(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(values);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
