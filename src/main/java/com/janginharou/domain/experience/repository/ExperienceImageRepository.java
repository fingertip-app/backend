package com.janginharou.domain.experience.repository;

import com.janginharou.domain.experience.entity.ExperienceImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExperienceImageRepository extends JpaRepository<ExperienceImage, Long> {
    List<ExperienceImage> findByExperienceIdOrderByDisplayOrderAsc(Long experienceId);
}