package com.janginharou.domain.ai.service;

import com.janginharou.domain.ai.dto.AiRecommendationRequest;
import com.janginharou.domain.ai.dto.AiRecommendationRequest.CompanionType;
import com.janginharou.domain.ai.dto.AiRecommendationRequest.ConversationMessage;
import com.janginharou.domain.ai.dto.AiRecommendationRequest.ConversationRole;
import com.janginharou.domain.ai.dto.AiRecommendationRequest.TimePreference;
import com.janginharou.domain.ai.dto.AiRecommendationResponse;
import com.janginharou.domain.experience.entity.Experience;
import com.janginharou.domain.experience.repository.ExperienceRepository;
import com.janginharou.global.client.FastApiClient;
import com.janginharou.global.client.dto.FastApiRecommendationRequest;
import com.janginharou.global.client.dto.FastApiRecommendationResponse;
import com.janginharou.global.client.dto.FastApiSourceResponse;
import com.janginharou.global.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private FastApiClient fastApiClient;

    @Mock
    private ExperienceRepository experienceRepository;

    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        recommendationService = new RecommendationService(fastApiClient, experienceRepository);
    }

    @Test
    void shouldRecommendExperiencesWithAiResponse() {
        // Arrange
        AiRecommendationRequest request = new AiRecommendationRequest(
                null,
                CompanionType.FRIEND,
                2,
                List.of("공예", "매듭"),
                null,
                null,
                null,
                null
        );

        FastApiRecommendationResponse aiResponse = new FastApiRecommendationResponse(
                "친구와 함께 전통 공예 체험을 추천합니다.",
                List.of(1L),
                Map.of("1", "전통 공예를 직접 체험할 수 있습니다"),
                List.of("공예", "매듭장"),
                List.of(),
                false,
                null
        );

        Experience experience = Experience.builder()
                .id(1L)
                .title("전통 매듭 만들기")
                .locationAddress("서울 종로구")
                .price(BigDecimal.valueOf(30000))
                .durationMinutes(60)
                .maxParticipants(10)
                .tags(List.of("공예", "매듭"))
                .build();

        when(fastApiClient.getRecommendations(any(FastApiRecommendationRequest.class)))
                .thenReturn(aiResponse);
        when(experienceRepository.findAllByIdWithImages(List.of(1L)))
                .thenReturn(List.of(experience));

        // Act
        AiRecommendationResponse response = recommendationService.recommend(request);

        // Assert
        assertThat(response.getAnswer()).isEqualTo(aiResponse.getAnswer());
        assertThat(response.isFallback()).isFalse();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getRecommendedExperiences()).hasSize(1)
                .extracting("title").contains("전통 매듭 만들기");
    }

    @Test
    void shouldBuildQueryWithAllOptionalFields() {
        // Arrange
        AiRecommendationRequest request = new AiRecommendationRequest(
                "친구랑 조용한 체험을 하고 싶어요",
                CompanionType.FRIEND,
                2,
                List.of("공예"),
                "서울",
                TimePreference.WEEKEND,
                List.of(
                        new ConversationMessage(ConversationRole.USER, "특별한 체험을 찾고 있어요"),
                        new ConversationMessage(ConversationRole.ASSISTANT, "공예 체험이 좋을까요?")
                ),
                "ko"
        );

        FastApiRecommendationResponse aiResponse = new FastApiRecommendationResponse(
                "체험을 추천합니다.",
                List.of(),
                Map.of(),
                List.of(),
                List.of(),
                false,
                null
        );

        when(fastApiClient.getRecommendations(any(FastApiRecommendationRequest.class)))
                .thenReturn(aiResponse);

        // Act
        recommendationService.recommend(request);

        // Assert
        ArgumentCaptor<FastApiRecommendationRequest> requestCaptor =
                ArgumentCaptor.forClass(FastApiRecommendationRequest.class);
        verify(fastApiClient).getRecommendations(requestCaptor.capture());
        FastApiRecommendationRequest capturedRequest = requestCaptor.getValue();

        assertThat(capturedRequest.getFreeText()).isEqualTo("친구랑 조용한 체험을 하고 싶어요");
        assertThat(capturedRequest.getCompanionType()).isEqualTo("FRIEND");
        assertThat(capturedRequest.getHeadCount()).isEqualTo(2);
        assertThat(capturedRequest.getInterests()).containsExactly("공예");
        assertThat(capturedRequest.getRegion()).isEqualTo("서울");
        assertThat(capturedRequest.getTimePreference()).isEqualTo("WEEKEND");
        assertThat(capturedRequest.getLocale()).isEqualTo("ko");
        assertThat(capturedRequest.getConversationHistory()).hasSize(2);
    }

    @Test
    void shouldReturnRecommendedTagsFromAiResponse() {
        // Arrange
        AiRecommendationRequest request = new AiRecommendationRequest(
                null,
                CompanionType.FRIEND,
                2,
                List.of("공예", "매듭"),
                null,
                null,
                null,
                null
        );

        FastApiRecommendationResponse aiResponse = new FastApiRecommendationResponse(
                "체험을 추천합니다.",
                List.of(1L),
                Map.of("1", "전통 공예 체험입니다"),
                List.of("공예", "전통", "매듭"),
                List.of(),
                false,
                null
        );

        Experience exp1 = Experience.builder()
                .id(1L)
                .title("체험1")
                .locationAddress("서울")
                .price(BigDecimal.valueOf(30000))
                .durationMinutes(60)
                .maxParticipants(10)
                .tags(List.of("공예"))
                .build();

        when(fastApiClient.getRecommendations(any(FastApiRecommendationRequest.class)))
                .thenReturn(aiResponse);
        when(experienceRepository.findAllByIdWithImages(List.of(1L)))
                .thenReturn(List.of(exp1));

        // Act
        AiRecommendationResponse response = recommendationService.recommend(request);

        // Assert
        assertThat(response.getRecommendedTags()).containsExactly("공예", "전통", "매듭");
    }

    @Test
    void shouldHandleBlankOptionalFieldsGracefully() {
        // Arrange
        AiRecommendationRequest request = new AiRecommendationRequest(
                "   ",  // blank freeText
                CompanionType.FRIEND,
                2,
                List.of("공예"),
                "  ",  // blank region
                null,
                null,
                null
        );

        FastApiRecommendationResponse aiResponse = new FastApiRecommendationResponse(
                "체험을 추천합니다.",
                List.of(),
                Map.of(),
                List.of(),
                List.of(),
                false,
                null
        );

        when(fastApiClient.getRecommendations(any(FastApiRecommendationRequest.class)))
                .thenReturn(aiResponse);

        // Act
        AiRecommendationResponse response = recommendationService.recommend(request);

        // Assert - service should handle blank fields without error
        assertThat(response).isNotNull();
        assertThat(response.isFallback()).isFalse();
    }

    @Test
    void shouldReturnExperiencesInOrderReturnedByAi() {
        // Arrange - Python AI now handles region filtering
        AiRecommendationRequest request = new AiRecommendationRequest(
                null,
                CompanionType.FRIEND,
                2,
                List.of("공예"),
                "서울",
                null,
                null,
                null
        );

        // Python AI already filtered by region and returns only Seoul experiences
        FastApiRecommendationResponse aiResponse = new FastApiRecommendationResponse(
                "서울의 체험을 추천합니다.",
                List.of(1L, 3L),
                Map.of("1", "첫 번째 추천", "3", "두 번째 추천"),
                List.of("공예"),
                List.of(),
                false,
                null
        );

        Experience exp1 = Experience.builder()
                .id(1L)
                .title("서울 체험 1")
                .locationAddress("서울 종로구")
                .price(BigDecimal.valueOf(30000))
                .durationMinutes(60)
                .maxParticipants(10)
                .tags(List.of("공예"))
                .build();

        Experience exp3 = Experience.builder()
                .id(3L)
                .title("서울 체험 3")
                .locationAddress("서울 강남구")
                .price(BigDecimal.valueOf(30000))
                .durationMinutes(60)
                .maxParticipants(10)
                .tags(List.of("공예"))
                .build();

        when(fastApiClient.getRecommendations(any(FastApiRecommendationRequest.class)))
                .thenReturn(aiResponse);
        when(experienceRepository.findAllByIdWithImages(List.of(1L, 3L)))
                .thenReturn(List.of(exp1, exp3));

        // Act
        AiRecommendationResponse response = recommendationService.recommend(request);

        // Assert - experiences should maintain AI's order
        assertThat(response.getRecommendedExperiences()).hasSize(2);
        assertThat(response.getRecommendedExperiences().get(0).getTitle()).isEqualTo("서울 체험 1");
        assertThat(response.getRecommendedExperiences().get(1).getTitle()).isEqualTo("서울 체험 3");
    }

    @Test
    void shouldReturnExperiencesWithReasonsFromAi() {
        // Arrange - Python AI now handles headcount filtering
        AiRecommendationRequest request = new AiRecommendationRequest(
                null,
                CompanionType.FRIEND,
                8,
                List.of("공예"),
                null,
                null,
                null,
                null
        );

        // Python AI already filtered by head count
        FastApiRecommendationResponse aiResponse = new FastApiRecommendationResponse(
                "8명이 참여 가능한 체험을 추천합니다.",
                List.of(1L),
                Map.of("1", "대인원이 함께 즐길 수 있는 공예 체험입니다"),
                List.of("공예"),
                List.of(),
                false,
                null
        );

        Experience largeGroup = Experience.builder()
                .id(1L)
                .title("대인원 체험")
                .locationAddress("서울")
                .price(BigDecimal.valueOf(30000))
                .durationMinutes(60)
                .maxParticipants(10)
                .tags(List.of("공예"))
                .build();

        when(fastApiClient.getRecommendations(any(FastApiRecommendationRequest.class)))
                .thenReturn(aiResponse);
        when(experienceRepository.findAllByIdWithImages(List.of(1L)))
                .thenReturn(List.of(largeGroup));

        // Act
        AiRecommendationResponse response = recommendationService.recommend(request);

        // Assert
        assertThat(response.getRecommendedExperiences()).hasSize(1);
        assertThat(response.getRecommendedExperiences().get(0).getMatchReason())
                .isEqualTo("대인원이 함께 즐길 수 있는 공예 체험입니다");
    }

    @Test
    void shouldReturnFallbackOnAiUnavailable() {
        // Arrange
        AiRecommendationRequest request = new AiRecommendationRequest(
                null,
                CompanionType.FRIEND,
                2,
                List.of("공예"),
                null,
                null,
                null,
                null
        );

        Experience fallbackExp = Experience.builder()
                .id(1L)
                .title("기본 체험")
                .locationAddress("서울")
                .price(BigDecimal.valueOf(30000))
                .durationMinutes(60)
                .maxParticipants(10)
                .tags(List.of("공예"))
                .isActive(true)
                .build();

        when(fastApiClient.getRecommendations(any(FastApiRecommendationRequest.class)))
                .thenThrow(new ExternalServiceException("AI unavailable", "AI_UNAVAILABLE"));
        when(experienceRepository.findByIsActiveTrue())
                .thenReturn(List.of(fallbackExp));

        // Act
        AiRecommendationResponse response = recommendationService.recommend(request);

        // Assert
        assertThat(response.isFallback()).isTrue();
        assertThat(response.getMessage()).isNotNull();
        assertThat(response.getRecommendedExperiences()).hasSize(1)
                .extracting("matchReason").contains("기본 활성 체험으로 추천");
    }

    @Test
    void shouldFilterAndOrderFallbackExperiences() {
        // Arrange
        AiRecommendationRequest request = new AiRecommendationRequest(
                null,
                CompanionType.FRIEND,
                8,
                List.of("공예"),
                null,
                null,
                null,
                null
        );

        Experience laterExperience = Experience.builder()
                .id(2L)
                .title("두 번째 체험")
                .locationAddress("서울")
                .price(BigDecimal.valueOf(30000))
                .durationMinutes(60)
                .maxParticipants(10)
                .tags(List.of("공예"))
                .isActive(true)
                .build();

        Experience insufficientCapacity = Experience.builder()
                .id(1L)
                .title("정원 부족 체험")
                .locationAddress("서울")
                .price(BigDecimal.valueOf(30000))
                .durationMinutes(60)
                .maxParticipants(5)
                .tags(List.of("공예"))
                .isActive(true)
                .build();

        Experience firstExperience = Experience.builder()
                .id(3L)
                .title("세 번째 체험")
                .locationAddress("서울")
                .price(BigDecimal.valueOf(30000))
                .durationMinutes(60)
                .maxParticipants(8)
                .tags(List.of("공예"))
                .isActive(true)
                .build();

        when(fastApiClient.getRecommendations(any(FastApiRecommendationRequest.class)))
                .thenThrow(new ExternalServiceException("AI unavailable", "AI_UNAVAILABLE"));
        when(experienceRepository.findByIsActiveTrue())
                .thenReturn(List.of(firstExperience, insufficientCapacity, laterExperience));

        // Act
        AiRecommendationResponse response = recommendationService.recommend(request);

        // Assert
        assertThat(response.getRecommendedExperiences())
                .extracting("id")
                .containsExactly(2L, 3L);
    }

    @Test
    void shouldThrowWhenNoFallbackExperienceCanFitHeadCount() {
        // Arrange
        AiRecommendationRequest request = new AiRecommendationRequest(
                null,
                CompanionType.FRIEND,
                8,
                List.of("공예"),
                null,
                null,
                null,
                null
        );

        Experience insufficientCapacity = Experience.builder()
                .id(1L)
                .title("정원 부족 체험")
                .locationAddress("서울")
                .price(BigDecimal.valueOf(30000))
                .durationMinutes(60)
                .maxParticipants(5)
                .tags(List.of("공예"))
                .isActive(true)
                .build();

        when(fastApiClient.getRecommendations(any(FastApiRecommendationRequest.class)))
                .thenThrow(new ExternalServiceException("AI unavailable", "AI_UNAVAILABLE"));
        when(experienceRepository.findByIsActiveTrue())
                .thenReturn(List.of(insufficientCapacity));

        // Act & Assert
        assertThatThrownBy(() -> recommendationService.recommend(request))
                .isInstanceOf(ExternalServiceException.class)
                .extracting(e -> ((ExternalServiceException) e).getErrorCode())
                .isEqualTo("AI_UNAVAILABLE");
    }

    @Test
    void shouldThrowWhenFallbackUnavailable() {
        // Arrange
        AiRecommendationRequest request = new AiRecommendationRequest(
                null,
                CompanionType.FRIEND,
                2,
                List.of("공예"),
                null,
                null,
                null,
                null
        );

        when(fastApiClient.getRecommendations(any(FastApiRecommendationRequest.class)))
                .thenThrow(new ExternalServiceException("AI unavailable", "AI_UNAVAILABLE"));
        when(experienceRepository.findByIsActiveTrue())
                .thenReturn(List.of());

        // Act & Assert
        assertThatThrownBy(() -> recommendationService.recommend(request))
                .isInstanceOf(ExternalServiceException.class)
                .extracting(e -> ((ExternalServiceException) e).getErrorCode())
                .isEqualTo("AI_UNAVAILABLE");
    }

    @Test
    void shouldReturnEmptyExperiencesWhenAiReturnsNoRecommendations() {
        // Arrange
        AiRecommendationRequest request = new AiRecommendationRequest(
                null,
                CompanionType.FRIEND,
                2,
                List.of("공예"),
                null,
                null,
                null,
                null
        );

        FastApiRecommendationResponse aiResponse = new FastApiRecommendationResponse(
                "매칭 체험이 없습니다.",
                List.of(),
                Map.of(),
                List.of(),
                List.of(),
                false,
                null
        );

        when(fastApiClient.getRecommendations(any(FastApiRecommendationRequest.class)))
                .thenReturn(aiResponse);

        // Act
        AiRecommendationResponse response = recommendationService.recommend(request);

        // Assert
        assertThat(response.isFallback()).isFalse();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getRecommendedExperiences()).isEmpty();
    }
}
