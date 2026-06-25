package com.janginharou.domain.experience.repository;

import com.janginharou.domain.experience.entity.Experience;
import com.janginharou.domain.experience.entity.ExperienceImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExperienceImageRepository extends JpaRepository<ExperienceImage, Long> {
    List<ExperienceImage> findByExperienceIdOrderByDisplayOrderAsc(Long experienceId);
    @Query("SELECT MAX(i.displayOrder) FROM ExperienceImage i WHERE i.experience.id = :experienceId")
    Integer findMaxDisplayOrderByExperienceId(@Param("experienceId") Long experienceId);
    void deleteAllByExperience(Experience experience);
    void deleteAllByExperienceId(Long experienceId);
}
