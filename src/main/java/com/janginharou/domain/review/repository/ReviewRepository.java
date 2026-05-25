package com.janginharou.domain.review.repository;

import com.janginharou.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByExperienceId(Long experienceId);

    List<Review> findByUserId(Long userId);

    List<Review> findByIsApprovedTrue();

    List<Review> findByExperienceIdAndIsApprovedTrue(Long experienceId);
}
