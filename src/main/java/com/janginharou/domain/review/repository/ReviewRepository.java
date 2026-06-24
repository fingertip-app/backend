package com.janginharou.domain.review.repository;

import com.janginharou.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
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
}
