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
import com.janginharou.global.client.dto.FastApiExplainResponse;
import com.janginharou.global.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
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

        FastApiExplainResponse aiResponse = new FastApiExplainResponse(
                "친구와 함께 전통 공예 체험을 추천합니다.",
                List.of(),
                List.of("공예", "매듭장"),
                List.of("공예")
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

        when(fastApiClient.explainCulture(anyString(), eq("ko")))
                .thenReturn(aiResponse);
        when(experienceRepository.findByTagsContainingAny(anyList()))
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

        FastApiExplainResponse aiResponse = new FastApiExplainResponse(
                "체험을 추천합니다.",
                List.of(),
                List.of(),
                List.of()
        );

        when(fastApiClient.explainCulture(anyString(), eq("ko")))
                .thenReturn(aiResponse);
        when(experienceRepository.findByTagsContainingAny(List.of("공예")))
                .thenReturn(List.of());

        // Act
        recommendationService.recommend(request);

        // Assert
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(fastApiClient).explainCulture(queryCaptor.capture(), anyString());
        String query = queryCaptor.getValue();

        assertThat(query).contains("친구");
        assertThat(query).contains("2명");
        assertThat(query).contains("서울");
        assertThat(query).contains("주말");
        assertThat(query).contains("공예");
        assertThat(query).contains("친구랑 조용한 체험을 하고 싶어요");
        assertThat(query).contains("USER: 특별한 체험을 찾고 있어요");
        assertThat(query).contains("ASSISTANT: 공예 체험이 좋을까요?");
    }

    @Test
    void shouldRemoveDuplicateTagsFromPool() {
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

        FastApiExplainResponse aiResponse = new FastApiExplainResponse(
                "체험을 추천합니다.",
                List.of(),
                List.of("공예", "전통"),
                List.of("공예")
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

        when(fastApiClient.explainCulture(anyString(), eq("ko")))
                .thenReturn(aiResponse);
        when(experienceRepository.findByTagsContainingAny(List.of("공예", "매듭", "전통")))
                .thenReturn(List.of(exp1));

        // Act
        AiRecommendationResponse response = recommendationService.recommend(request);

        // Assert
        // interests + matchingKeywords + recommendedCategories 중복 제거
        assertThat(response.getRecommendedTags()).containsExactly("공예", "매듭", "전통");
    }

    @Test
    void shouldExcludeBlankOptionalFields() {
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

        FastApiExplainResponse aiResponse = new FastApiExplainResponse(
                "체험을 추천합니다.",
                List.of(),
                List.of(),
                List.of()
        );

        when(fastApiClient.explainCulture(anyString(), eq("ko")))
                .thenReturn(aiResponse);
        when(experienceRepository.findByTagsContainingAny(List.of("공예")))
                .thenReturn(List.of());

        // Act
        recommendationService.recommend(request);

        // Assert
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(fastApiClient).explainCulture(queryCaptor.capture(), org.mockito.ArgumentMatchers.anyString());
        String query = queryCaptor.getValue();

        assertThat(query).doesNotContain("추가 요청");
        assertThat(query).doesNotContain("에서");
    }

    @Test
    void shouldFilterByRegion() {
        // Arrange
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

        FastApiExplainResponse aiResponse = new FastApiExplainResponse(
                "체험을 추천합니다.",
                List.of(),
                List.of(),
                List.of()
        );

        Experience seoulExp = Experience.builder()
                .id(1L)
                .title("서울 체험")
                .locationAddress("서울 종로구")
                .price(BigDecimal.valueOf(30000))
                .durationMinutes(60)
                .maxParticipants(10)
                .tags(List.of("공예"))
                .build();

        Experience busan = Experience.builder()
                .id(2L)
                .title("부산 체험")
                .locationAddress("부산 해운대구")
                .price(BigDecimal.valueOf(30000))
                .durationMinutes(60)
                .maxParticipants(10)
                .tags(List.of("공예"))
                .build();

        when(fastApiClient.explainCulture(anyString(), eq("ko")))
                .thenReturn(aiResponse);
        when(experienceRepository.findByTagsContainingAny(List.of("공예")))
                .thenReturn(List.of(seoulExp, busan));

        // Act
        AiRecommendationResponse response = recommendationService.recommend(request);

        // Assert
        assertThat(response.getRecommendedExperiences()).hasSize(1)
                .extracting("location").contains("서울 종로구");
    }

    @Test
    void shouldFilterByHeadCount() {
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

        FastApiExplainResponse aiResponse = new FastApiExplainResponse(
                "체험을 추천합니다.",
                List.of(),
                List.of(),
                List.of()
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

        Experience smallGroup = Experience.builder()
                .id(2L)
                .title("소인원 체험")
                .locationAddress("서울")
                .price(BigDecimal.valueOf(30000))
                .durationMinutes(60)
                .maxParticipants(5)
                .tags(List.of("공예"))
                .build();

        when(fastApiClient.explainCulture(anyString(), eq("ko")))
                .thenReturn(aiResponse);
        when(experienceRepository.findByTagsContainingAny(List.of("공예")))
                .thenReturn(List.of(largeGroup, smallGroup));

        // Act
        AiRecommendationResponse response = recommendationService.recommend(request);

        // Assert
        assertThat(response.getRecommendedExperiences()).hasSize(1)
                .extracting("title").contains("대인원 체험");
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

        when(fastApiClient.explainCulture(anyString(), eq("ko")))
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

        when(fastApiClient.explainCulture(anyString(), eq("ko")))
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

        when(fastApiClient.explainCulture(anyString(), eq("ko")))
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

        when(fastApiClient.explainCulture(anyString(), eq("ko")))
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
    void shouldReturnEmptyExperiencesWhenNoTagMatch() {
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

        FastApiExplainResponse aiResponse = new FastApiExplainResponse(
                "매칭 체험이 없습니다.",
                List.of(),
                List.of(),
                List.of()
        );

        when(fastApiClient.explainCulture(anyString(), eq("ko")))
                .thenReturn(aiResponse);
        when(experienceRepository.findByTagsContainingAny(List.of("공예")))
                .thenReturn(List.of());

        // Act
        AiRecommendationResponse response = recommendationService.recommend(request);

        // Assert
        assertThat(response.isFallback()).isFalse();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getRecommendedExperiences()).isEmpty();
    }
}
