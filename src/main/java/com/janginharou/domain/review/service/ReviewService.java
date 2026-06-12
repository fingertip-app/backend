package com.janginharou.domain.review.service;

import com.janginharou.domain.experience.repository.ExperienceRepository;
import com.janginharou.domain.reservation.repository.ReservationRepository;
import com.janginharou.domain.review.dto.ReviewRequest;
import com.janginharou.domain.review.entity.Review;
import com.janginharou.domain.review.repository.ReviewRepository;
import com.janginharou.domain.user.repository.UserRepository;
import com.janginharou.global.client.FastApiClient;
import com.janginharou.global.client.dto.FastApiSummarizeResponse;
import com.janginharou.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final FastApiClient fastApiClient;
    private final UserRepository userRepository;
    private final ExperienceRepository experienceRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
    }

    @Transactional(readOnly = true)
    public List<Review> getReviewsByExperienceId(Long experienceId) {
        return reviewRepository.findByExperienceId(experienceId);
    }

    @Transactional(readOnly = true)
    public List<Review> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    @Transactional
    public Review createReview(Long userId, ReviewRequest request) {
        // TODO: 후기 생성 처리 (예약 완료된 Reservation에 대해서만 작성 가능)
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        var experience = experienceRepository.findById(request.getExperienceId())
                .orElseThrow(() -> new ResourceNotFoundException("Experience", "id", request.getExperienceId()));

        Review review = Review.builder()
                .user(user)
                .experience(experience)
                .rating(request.getRating())
                .content(request.getContent())
                .newLearnings(request.getNewKnowledge())
                .build();

        Review savedReview = reviewRepository.save(review);
        applySummaryIfAvailable(savedReview);

        return savedReview;
    }

    @Transactional
    public Review updateReview(Long reviewId, Review updateData) {
        // TODO: 후기 수정 처리 (본인만 수정 가능)
        Review review = getReviewById(reviewId);
        return review;
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    private void applySummaryIfAvailable(Review review) {
        if (review.getContent() == null || review.getContent().isBlank()) {
            return;
        }

        try {
            FastApiSummarizeResponse response = fastApiClient.summarizeReview(review.getContent(), "ko");
            review.applySummary(response.getSummary(), response.getSentimentScore(), response.getKeywords());
        } catch (ExternalServiceException e) {
            log.warn("Failed to apply AI summary to review {}: errorCode={}", review.getId(), e.getErrorCode());
        }
    }
}
