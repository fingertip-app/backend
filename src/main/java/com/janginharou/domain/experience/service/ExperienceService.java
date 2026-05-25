package com.janginharou.domain.experience.service;

import com.janginharou.domain.experience.entity.Experience;
import com.janginharou.domain.experience.repository.ExperienceRepository;
import com.janginharou.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExperienceService {

    private final ExperienceRepository experienceRepository;

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
    public List<Experience> getUpcomingExperiences() {
        return experienceRepository.findByStartDateTimeGreaterThanAndIsActiveTrue(LocalDateTime.now());
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
}
