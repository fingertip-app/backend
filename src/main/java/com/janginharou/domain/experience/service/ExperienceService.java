package com.janginharou.domain.experience.service;

import com.janginharou.domain.experience.dto.ExperienceResponse;
import com.janginharou.domain.experience.entity.Experience;
import com.janginharou.domain.experience.entity.ExperienceSchedule;
import com.janginharou.domain.experience.repository.ExperienceRepository;
import com.janginharou.domain.experience.repository.ExperienceScheduleRepository;
import com.janginharou.domain.reservation.entity.ReservationStatus;
import com.janginharou.domain.reservation.repository.ReservationRepository;
import com.janginharou.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final ExperienceScheduleRepository experienceScheduleRepository;
    private final ReservationRepository reservationRepository;

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
    public List<Experience> getActiveExperiences() {
        return experienceRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<ExperienceResponse> getActiveExperienceResponses() {
        return experienceRepository.findByIsActiveTrue()
                .stream()
                .map(ExperienceResponse::from)
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
                .createdAt(experience.getCreatedAt())
                .updatedAt(experience.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ExperienceResponse> getUpcomingExperiences() {
        return getActiveExperienceResponses();
    }

    @Transactional
    public Experience createExperience(Experience experience) {
        // TODO: 체험 프로그램 생성 처리 (이미지 S3 업로드, 예약 가능 상태 초기화 등)
        return experienceRepository.save(experience);
    }

    @Transactional
    public Experience updateExperience(Long experienceId, Experience updateData) {
        // TODO: 체험 프로그램 수정 처리
        Experience experience = getExperienceById(experienceId);
        return experience;
    }

    @Transactional
    public void deleteExperience(Long experienceId) {
        // TODO: 체험 프로그램 삭제 처리 (관련 예약 처리)
        experienceRepository.deleteById(experienceId);
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
}
