package com.janginharou.domain.experience.repository;

import com.janginharou.domain.experience.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    List<Experience> findByArtisanId(Long artisanId);

    List<Experience> findByIsActiveTrue();

    List<Experience> findByStartDateTimeGreaterThanAndIsActiveTrue(LocalDateTime dateTime);

    List<Experience> findByArtisanIdAndIsActiveTrue(Long artisanId);
}
