package com.janginharou.domain.experience.repository;

import com.janginharou.domain.experience.entity.ExperienceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperienceScheduleRepository extends JpaRepository<ExperienceSchedule, Long> {

    List<ExperienceSchedule> findByExperienceIdAndIsActiveTrue(Long experienceId);
}
