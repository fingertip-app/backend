package com.janginharou.domain.review.service;

import com.janginharou.domain.review.entity.Review;
import com.janginharou.domain.review.repository.ReviewRepository;
import com.janginharou.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

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
    public List<Review> getApprovedReviewsByExperienceId(Long experienceId) {
        return reviewRepository.findByExperienceIdAndIsApprovedTrue(experienceId);
    }

    @Transactional(readOnly = true)
    public List<Review> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    @Transactional
    public Review createReview(Review review) {
        // TODO: 후기 생성 처리 (관리자 승인 대기 상태로 시작)
        review = Review.builder()
                .user(review.getUser())
                .experience(review.getExperience())
                .rating(review.getRating())
                .content(review.getContent())
                .imageUrl(review.getImageUrl())
                .newKnowledge(review.getNewKnowledge())
                .isApproved(false)
                .build();
        return reviewRepository.save(review);
    }

    @Transactional
    public Review updateReview(Long reviewId, Review updateData) {
        // TODO: 후기 수정 처리 (본인만 수정 가능)
        Review review = getReviewById(reviewId);
        return review;
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        // TODO: 후기 삭제 처리
        reviewRepository.deleteById(reviewId);
    }

    @Transactional
    public Review approveReview(Long reviewId) {
        // TODO: 관리자가 후기 승인 처리
        Review review = getReviewById(reviewId);
        return review;
    }
}
