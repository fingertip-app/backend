package com.janginharou.domain.review.repository;

import com.janginharou.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByExperienceId(Long experienceId);

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
     * Map의 key: experienceId
     * Map의 value: {avgRating: Double, reviewCount: Long}
     */
    @Query("""
            SELECT r.experience.id as experienceId,
                   AVG(r.rating) as avgRating,
                   COUNT(r) as reviewCount
            FROM Review r
            WHERE r.experience.id IN :experienceIds
            GROUP BY r.experience.id
            """)
    List<Map<String, Object>> getReviewStatsByExperienceIds(@Param("experienceIds") List<Long> experienceIds);
}
