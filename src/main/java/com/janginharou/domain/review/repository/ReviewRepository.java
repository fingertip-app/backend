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

    Optional<Review> findByBookingId(Long bookingId);
}
