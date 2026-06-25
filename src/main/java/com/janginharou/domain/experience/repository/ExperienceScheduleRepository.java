package com.janginharou.domain.experience.repository;

import com.janginharou.domain.experience.entity.ExperienceSchedule;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExperienceScheduleRepository extends JpaRepository<ExperienceSchedule, Long> {

    List<ExperienceSchedule> findByExperienceIdAndIsActiveTrue(Long experienceId);

    void deleteByExperienceId(Long experienceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ExperienceSchedule s join fetch s.experience where s.id = :id")
    Optional<ExperienceSchedule> findByIdForUpdate(@Param("id") Long id);
}
