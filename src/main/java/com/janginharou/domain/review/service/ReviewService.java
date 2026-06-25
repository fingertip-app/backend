package com.janginharou.domain.review.service;

import com.janginharou.domain.experience.repository.ExperienceRepository;
import com.janginharou.domain.reservation.entity.Reservation;
import com.janginharou.domain.reservation.entity.ReservationStatus;
import com.janginharou.domain.reservation.repository.ReservationRepository;
import com.janginharou.domain.review.dto.ReviewRequest;
import com.janginharou.domain.review.dto.ReviewSummaryRequest;
import com.janginharou.domain.review.dto.ReviewSummaryResponse;
import com.janginharou.domain.review.entity.Review;
import com.janginharou.domain.review.repository.ReviewRepository;
import com.janginharou.domain.user.repository.UserRepository;
import com.janginharou.global.client.FastApiClient;
import com.janginharou.global.client.dto.FastApiSummarizeResponse;
import com.janginharou.global.exception.ExternalServiceException;
import com.janginharou.global.exception.InvalidRequestException;
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

    private static final List<ReservationStatus> REVIEWABLE_RESERVATION_STATUSES = List.of(
            ReservationStatus.COMPLETED
    );

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
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        var experience = experienceRepository.findById(request.getExperienceId())
                .orElseThrow(() -> new ResourceNotFoundException("Experience", "id", request.getExperienceId()));
        Reservation reservation = resolveReviewableReservation(userId, request);

        if (reviewRepository.existsByReservationId(reservation.getId())) {
            throw new InvalidRequestException("Review already exists for this reservation");
        }

        Review review = Review.builder()
                .reservation(reservation)
                .user(user)
                .experience(experience)
                .rating(request.getRating())
                .content(request.getContent())
                .newLearnings(request.getNewKnowledge())
                .imageUrls(toImageUrls(request.getImageUrl()))
                .build();

        Review savedReview = reviewRepository.save(review);
        applySummaryIfAvailable(savedReview);

        return savedReview;
    }

    @Transactional
    public Review updateReview(Long userId, Long reviewId, ReviewRequest request) {
        Review review = getReviewById(reviewId);
        validateOwner(review, userId);
        if (!review.getExperience().getId().equals(request.getExperienceId())) {
            throw new InvalidRequestException("Review experience cannot be changed");
        }

        review.update(
                request.getRating(),
                request.getContent(),
                request.getNewKnowledge(),
                toImageUrls(request.getImageUrl())
        );
        applySummaryIfAvailable(review);
        return review;
    }

    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = getReviewById(reviewId);
        validateOwner(review, userId);
        reviewRepository.delete(review);
    }

    private Reservation resolveReviewableReservation(Long userId, ReviewRequest request) {
        if (request.getReservationId() != null) {
            Reservation reservation = reservationRepository.findById(request.getReservationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", request.getReservationId()));
            validateReviewableReservation(reservation, userId, request.getExperienceId());
            return reservation;
        }

        return reservationRepository.findFirstByUserIdAndExperienceIdAndStatusInOrderByCreatedAtDesc(
                        userId,
                        request.getExperienceId(),
                        REVIEWABLE_RESERVATION_STATUSES
                )
                .orElseThrow(() -> new InvalidRequestException("Completed reservation is required to create a review"));
    }

    private void validateReviewableReservation(Reservation reservation, Long userId, Long experienceId) {
        if (!reservation.getUser().getId().equals(userId)) {
            throw new InvalidRequestException("Reservation does not belong to user");
        }
        if (!reservation.getExperience().getId().equals(experienceId)) {
            throw new InvalidRequestException("Reservation does not belong to experience");
        }
        if (!REVIEWABLE_RESERVATION_STATUSES.contains(reservation.getStatus())) {
            throw new InvalidRequestException("Completed reservation is required to create a review");
        }
    }

    private void validateOwner(Review review, Long userId) {
        if (!review.getUser().getId().equals(userId)) {
            throw new InvalidRequestException("Review does not belong to user");
        }
    }

    private List<String> toImageUrls(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return List.of();
        }
        return List.of(imageUrl.trim());
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

    public ReviewSummaryResponse summarizeReview(ReviewSummaryRequest request) {
        FastApiSummarizeResponse response = fastApiClient.summarizeReview(
                request.getContent(),
                request.getLocale() != null ? request.getLocale() : "ko"
        );

        return ReviewSummaryResponse.builder()
                .summary(response.getSummary())
                .sentimentScore(response.getSentimentScore().doubleValue())
                .keywords(response.getKeywords())
                .build();
    }
}
