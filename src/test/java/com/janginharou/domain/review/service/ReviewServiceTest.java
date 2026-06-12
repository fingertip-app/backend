package com.janginharou.domain.review.service;

import com.janginharou.domain.experience.entity.Experience;
import com.janginharou.domain.experience.repository.ExperienceRepository;
import com.janginharou.domain.reservation.repository.ReservationRepository;
import com.janginharou.domain.review.dto.ReviewRequest;
import com.janginharou.domain.review.entity.Review;
import com.janginharou.domain.review.repository.ReviewRepository;
import com.janginharou.domain.user.entity.User;
import com.janginharou.domain.user.repository.UserRepository;
import com.janginharou.global.client.FastApiClient;
import com.janginharou.global.client.dto.FastApiSummarizeResponse;
import com.janginharou.global.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private FastApiClient fastApiClient;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExperienceRepository experienceRepository;

    @Mock
    private ReservationRepository reservationRepository;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(
                reviewRepository,
                fastApiClient,
                userRepository,
                experienceRepository,
                reservationRepository
        );
    }

    @Test
    void savesReviewWithSummaryFromAi() {
        User user = User.builder().id(1L).build();
        Experience experience = Experience.builder().id(100L).build();
        ReviewRequest request = ReviewRequest.builder()
                .experienceId(100L)
                .rating(5)
                .content("정말 좋은 체험이었습니다!")
                .newKnowledge("많은 것을 배웠습니다")
                .build();

        Review unsavedReview = Review.builder()
                .user(user)
                .experience(experience)
                .rating(request.getRating())
                .content(request.getContent())
                .newLearnings(request.getNewKnowledge())
                .build();

        Review savedReview = Review.builder()
                .id(1L)
                .user(user)
                .experience(experience)
                .rating(request.getRating())
                .content(request.getContent())
                .newLearnings(request.getNewKnowledge())
                .build();

        FastApiSummarizeResponse aiResponse = new FastApiSummarizeResponse(
                "체험 요약 내용",
                BigDecimal.valueOf(0.85),
                List.of("체험", "배움")
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(experienceRepository.findById(100L)).thenReturn(Optional.of(experience));
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);
        when(fastApiClient.summarizeReview("정말 좋은 체험이었습니다!", "ko"))
                .thenReturn(aiResponse);

        Review result = reviewService.createReview(1L, request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getContent()).isEqualTo("정말 좋은 체험이었습니다!");
        assertThat(result.getSummary()).isEqualTo("체험 요약 내용");
        assertThat(result.getSentimentScore()).isEqualTo(BigDecimal.valueOf(0.85));
        assertThat(result.getKeywords()).containsExactly("체험", "배움");
        verify(fastApiClient).summarizeReview("정말 좋은 체험이었습니다!", "ko");
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void maintainsOriginalContentWhenAiFails() {
        User user = User.builder().id(1L).build();
        Experience experience = Experience.builder().id(100L).build();
        ReviewRequest request = ReviewRequest.builder()
                .experienceId(100L)
                .rating(4)
                .content("좋은 경험이었습니다")
                .newKnowledge("많이 배웠습니다")
                .build();

        Review savedReview = Review.builder()
                .id(2L)
                .user(user)
                .experience(experience)
                .rating(request.getRating())
                .content(request.getContent())
                .newLearnings(request.getNewKnowledge())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(experienceRepository.findById(100L)).thenReturn(Optional.of(experience));
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);
        when(fastApiClient.summarizeReview(anyString(), anyString()))
                .thenThrow(new ExternalServiceException("AI service unavailable", "AI_UNAVAILABLE"));

        Review result = reviewService.createReview(1L, request);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getContent()).isEqualTo("좋은 경험이었습니다");
        assertThat(result.getSummary()).isNull();
        assertThat(result.getSentimentScore()).isNull();
        assertThat(result.getKeywords()).isNull();
    }

    @Test
    void doesNotCallAiWhenContentIsBlank() {
        User user = User.builder().id(1L).build();
        Experience experience = Experience.builder().id(100L).build();
        ReviewRequest request = ReviewRequest.builder()
                .experienceId(100L)
                .rating(3)
                .content("")
                .newKnowledge("배운 것들")
                .build();

        Review savedReview = Review.builder()
                .id(3L)
                .user(user)
                .experience(experience)
                .rating(request.getRating())
                .content(request.getContent())
                .newLearnings(request.getNewKnowledge())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(experienceRepository.findById(100L)).thenReturn(Optional.of(experience));
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        Review result = reviewService.createReview(1L, request);

        assertThat(result.getId()).isEqualTo(3L);
        verify(fastApiClient, never()).summarizeReview(anyString(), anyString());
    }

    @Test
    void updatesReviewFieldsWithAiResponse() {
        User user = User.builder().id(1L).build();
        Experience experience = Experience.builder().id(100L).build();
        ReviewRequest request = ReviewRequest.builder()
                .experienceId(100L)
                .rating(5)
                .content("정말 훌륭한 체험입니다!")
                .newKnowledge("전통 문화를 배웠습니다")
                .build();

        Review savedReview = Review.builder()
                .id(4L)
                .user(user)
                .experience(experience)
                .rating(request.getRating())
                .content(request.getContent())
                .newLearnings(request.getNewKnowledge())
                .build();

        FastApiSummarizeResponse aiResponse = new FastApiSummarizeResponse(
                "전통 체험의 요약",
                BigDecimal.valueOf(0.92),
                List.of("전통", "문화", "배움")
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(experienceRepository.findById(100L)).thenReturn(Optional.of(experience));
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);
        when(fastApiClient.summarizeReview(anyString(), anyString()))
                .thenReturn(aiResponse);

        Review result = reviewService.createReview(1L, request);

        assertThat(result.getId()).isEqualTo(4L);
        assertThat(result.getSummary()).isEqualTo("전통 체험의 요약");
        assertThat(result.getSentimentScore()).isEqualTo(BigDecimal.valueOf(0.92));
        assertThat(result.getKeywords()).containsExactly("전통", "문화", "배움");
        verify(reviewRepository).save(any(Review.class));
    }
}
