package com.janginharou.domain.review.repository;

import com.janginharou.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByExperienceId(Long experienceId);

    @Modifying
    @Query(value = """
            DELETE FROM review_image_urls
            WHERE review_id IN (
                SELECT id FROM reviews WHERE experience_id = :experienceId
            )
            """, nativeQuery = true)
    void deleteImageUrlsByExperienceId(@Param("experienceId") Long experienceId);

    @Modifying
    @Query(value = """
            DELETE FROM review_keywords
            WHERE review_id IN (
                SELECT id FROM reviews WHERE experience_id = :experienceId
            )
            """, nativeQuery = true)
    void deleteKeywordsByExperienceId(@Param("experienceId") Long experienceId);

    default void deleteCollectionsByExperienceId(Long experienceId) {
        deleteImageUrlsByExperienceId(experienceId);
        deleteKeywordsByExperienceId(experienceId);
    }

    @Modifying
    @Query("DELETE FROM Review r WHERE r.experience.id = :experienceId")
    void deleteByExperienceId(@Param("experienceId") Long experienceId);

    List<Review> findByUserId(Long userId);

    long countByUserId(Long userId);

    Optional<Review> findByReservationId(Long reservationId);

    boolean existsByReservationId(Long reservationId);

    /**
     * 체험의 평균 평점 조회
     */
    default Double getAverageRating(Long experienceId) {
        List<Review> reviews = findByExperienceId(experienceId);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);
    }

    /**
     * 체험의 리뷰 개수
     */
    default Long getReviewCount(Long experienceId) {
        return (long) findByExperienceId(experienceId).size();
    }

    /**
     * 여러 체험의 평점/리뷰수를 한 번에 조회 (N+1 방지)
     */
    @Query("""
            SELECT r.experience.id as experienceId,
                   AVG(r.rating) as avgRating,
                   COUNT(r) as reviewCount
            FROM Review r
            WHERE r.experience.id IN :experienceIds
            GROUP BY r.experience.id
            """)
    List<ReviewStats> getReviewStatsByExperienceIds(@Param("experienceIds") List<Long> experienceIds);

    interface ReviewStats {
        Long getExperienceId();
        Double getAvgRating();
        Long getReviewCount();
    }
}
