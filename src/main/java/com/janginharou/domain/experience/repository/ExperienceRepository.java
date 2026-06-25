package com.janginharou.domain.experience.repository;

import com.janginharou.domain.experience.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    List<Experience> findByArtisanId(Long artisanId);

    List<Experience> findByIsActiveTrue();

    @Query("""
        SELECT DISTINCT e
        FROM Experience e
        JOIN ExperienceSchedule s ON s.experience = e
        WHERE s.scheduledAt > :dateTime
          AND s.isActive = true
          AND e.isActive = true
        """)
    List<Experience> findByStartDateTimeGreaterThanAndIsActiveTrue(
            @Param("dateTime") LocalDateTime dateTime
    );

    List<Experience> findByArtisanIdAndIsActiveTrue(Long artisanId);

    @Modifying
    @Query(value = "DELETE FROM experience_supported_languages WHERE experience_id = :experienceId", nativeQuery = true)
    void deleteSupportedLanguagesByExperienceId(@Param("experienceId") Long experienceId);

    @Modifying
    @Query(value = "DELETE FROM experience_tags WHERE experience_id = :experienceId", nativeQuery = true)
    void deleteTagsByExperienceId(@Param("experienceId") Long experienceId);

    @Modifying
    @Query("DELETE FROM Experience e WHERE e.id = :experienceId")
    void deleteByIdBulk(@Param("experienceId") Long experienceId);

    @Query("SELECT DISTINCT e FROM Experience e JOIN e.tags t WHERE t = :tag AND e.isActive = true")
    List<Experience> findByTagsContaining(@Param("tag") String tag);

    @Query("""
        SELECT e FROM Experience e
        WHERE e.isActive = true AND EXISTS (
            SELECT 1 FROM e.tags t
            WHERE t IN :tags
        )
        ORDER BY (
            SELECT COUNT(*) FROM e.tags t
            WHERE t IN :tags
        ) DESC, e.id ASC
        """)
    List<Experience> findByTagsContainingAny(@Param("tags") List<String> tags);

    @Query("""
        SELECT DISTINCT e
        FROM Experience e
        LEFT JOIN FETCH e.images
        WHERE e.id IN :ids
        """)
    List<Experience> findAllByIdWithImages(@Param("ids") List<Long> ids);
}
