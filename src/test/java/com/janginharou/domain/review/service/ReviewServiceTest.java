package com.janginharou.domain.review.service;

import com.janginharou.domain.experience.entity.Experience;
import com.janginharou.domain.experience.repository.ExperienceRepository;
import com.janginharou.domain.reservation.entity.Reservation;
import com.janginharou.domain.reservation.entity.ReservationStatus;
import com.janginharou.domain.reservation.repository.ReservationRepository;
import com.janginharou.domain.review.dto.ReviewRequest;
import com.janginharou.domain.review.entity.Review;
import com.janginharou.domain.review.repository.ReviewRepository;
import com.janginharou.domain.user.entity.User;
import com.janginharou.domain.user.repository.UserRepository;
import com.janginharou.global.client.FastApiClient;
import com.janginharou.global.client.dto.FastApiSummarizeResponse;
import com.janginharou.global.exception.ExternalServiceException;
import com.janginharou.global.exception.InvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
        Reservation reservation = completedReservation(10L, user, experience);
        ReviewRequest request = ReviewRequest.builder()
                .experienceId(100L)
                .rating(5)
                .content("정말 좋은 체험이었습니다!")
                .newKnowledge("많은 것을 배웠습니다")
                .build();

        Review savedReview = Review.builder()
                .id(1L)
                .reservation(reservation)
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
        when(reservationRepository.findFirstByUserIdAndExperienceIdAndStatusInOrderByCreatedAtDesc(
                eq(1L),
                eq(100L),
                eq(List.of(ReservationStatus.COMPLETED))
        )).thenReturn(Optional.of(reservation));
        when(reviewRepository.existsByReservationId(reservation.getId())).thenReturn(false);
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
        Reservation reservation = completedReservation(11L, user, experience);
        ReviewRequest request = ReviewRequest.builder()
                .experienceId(100L)
                .rating(4)
                .content("좋은 경험이었습니다")
                .newKnowledge("많이 배웠습니다")
                .build();

        Review savedReview = Review.builder()
                .id(2L)
                .reservation(reservation)
                .user(user)
                .experience(experience)
                .rating(request.getRating())
                .content(request.getContent())
                .newLearnings(request.getNewKnowledge())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(experienceRepository.findById(100L)).thenReturn(Optional.of(experience));
        when(reservationRepository.findFirstByUserIdAndExperienceIdAndStatusInOrderByCreatedAtDesc(any(), any(), any()))
                .thenReturn(Optional.of(reservation));
        when(reviewRepository.existsByReservationId(reservation.getId())).thenReturn(false);
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
        Reservation reservation = completedReservation(12L, user, experience);
        ReviewRequest request = ReviewRequest.builder()
                .experienceId(100L)
                .rating(3)
                .content("")
                .newKnowledge("배운 것들")
                .build();

        Review savedReview = Review.builder()
                .id(3L)
                .reservation(reservation)
                .user(user)
                .experience(experience)
                .rating(request.getRating())
                .content(request.getContent())
                .newLearnings(request.getNewKnowledge())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(experienceRepository.findById(100L)).thenReturn(Optional.of(experience));
        when(reservationRepository.findFirstByUserIdAndExperienceIdAndStatusInOrderByCreatedAtDesc(any(), any(), any()))
                .thenReturn(Optional.of(reservation));
        when(reviewRepository.existsByReservationId(reservation.getId())).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        Review result = reviewService.createReview(1L, request);

        assertThat(result.getId()).isEqualTo(3L);
        verify(fastApiClient, never()).summarizeReview(anyString(), anyString());
    }

    @Test
    void updatesReviewFieldsWithAiResponse() {
        User user = User.builder().id(1L).build();
        Experience experience = Experience.builder().id(100L).build();
        Reservation reservation = completedReservation(13L, user, experience);
        ReviewRequest request = ReviewRequest.builder()
                .experienceId(100L)
                .rating(5)
                .content("정말 훌륭한 체험입니다!")
                .newKnowledge("전통 문화를 배웠습니다")
                .build();

        Review savedReview = Review.builder()
                .id(4L)
                .reservation(reservation)
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
        when(reservationRepository.findFirstByUserIdAndExperienceIdAndStatusInOrderByCreatedAtDesc(any(), any(), any()))
                .thenReturn(Optional.of(reservation));
        when(reviewRepository.existsByReservationId(reservation.getId())).thenReturn(false);
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

    @Test
    void rejectsReviewWhenReviewableReservationDoesNotExist() {
        User user = User.builder().id(1L).build();
        Experience experience = Experience.builder().id(100L).build();
        ReviewRequest request = ReviewRequest.builder()
                .experienceId(100L)
                .rating(5)
                .content("예약 없이 작성하려는 후기")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(experienceRepository.findById(100L)).thenReturn(Optional.of(experience));
        when(reservationRepository.findFirstByUserIdAndExperienceIdAndStatusInOrderByCreatedAtDesc(
                eq(1L),
                eq(100L),
                eq(List.of(ReservationStatus.COMPLETED))
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(1L, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Completed reservation is required to create a review");

        verify(reviewRepository, never()).save(any(Review.class));
        verify(fastApiClient, never()).summarizeReview(anyString(), anyString());
    }

    @Test
    void updatesReviewOnlyForOwner() {
        User user = User.builder().id(1L).build();
        Experience experience = Experience.builder().id(100L).build();
        Reservation reservation = completedReservation(20L, user, experience);
        Review review = Review.builder()
                .id(30L)
                .reservation(reservation)
                .user(user)
                .experience(experience)
                .rating(3)
                .content("기존 후기")
                .newLearnings("기존 배움")
                .build();
        ReviewRequest request = ReviewRequest.builder()
                .experienceId(100L)
                .rating(5)
                .content("수정된 후기")
                .newKnowledge("수정된 배움")
                .imageUrl("https://example.com/review.png")
                .build();
        FastApiSummarizeResponse aiResponse = new FastApiSummarizeResponse(
                "수정 요약",
                BigDecimal.valueOf(0.7),
                List.of("수정")
        );

        when(reviewRepository.findById(30L)).thenReturn(Optional.of(review));
        when(fastApiClient.summarizeReview("수정된 후기", "ko")).thenReturn(aiResponse);

        Review result = reviewService.updateReview(1L, 30L, request);

        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getContent()).isEqualTo("수정된 후기");
        assertThat(result.getNewLearnings()).isEqualTo("수정된 배움");
        assertThat(result.getImageUrls()).containsExactly("https://example.com/review.png");
        assertThat(result.getSummary()).isEqualTo("수정 요약");
    }

    @Test
    void rejectsDeleteWhenUserIsNotOwner() {
        User owner = User.builder().id(1L).build();
        Experience experience = Experience.builder().id(100L).build();
        Reservation reservation = completedReservation(21L, owner, experience);
        Review review = Review.builder()
                .id(31L)
                .reservation(reservation)
                .user(owner)
                .experience(experience)
                .rating(4)
                .content("후기")
                .build();

        when(reviewRepository.findById(31L)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.deleteReview(99L, 31L))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Review does not belong to user");

        verify(reviewRepository, never()).delete(any(Review.class));
    }

    private Reservation completedReservation(Long id, User user, Experience experience) {
        return Reservation.builder()
                .id(id)
                .user(user)
                .experience(experience)
                .status(ReservationStatus.COMPLETED)
                .build();
    }
}
