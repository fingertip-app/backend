package com.janginharou.domain.ai.service;

import com.janginharou.domain.ai.dto.ExplainRequest;
import com.janginharou.domain.ai.dto.ExplainResponse;
import com.janginharou.domain.experience.entity.Experience;
import com.janginharou.domain.experience.repository.ExperienceRepository;
import com.janginharou.global.client.FastApiClient;
import com.janginharou.global.client.dto.FastApiExplainResponse;
import com.janginharou.global.client.dto.FastApiSourceResponse;
import com.janginharou.global.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExplainServiceTest {

    @Mock
    private FastApiClient fastApiClient;

    @Mock
    private ExperienceRepository experienceRepository;

    private ExplainService explainService;

    @BeforeEach
    void setUp() {
        explainService = new ExplainService(fastApiClient, experienceRepository);
    }

    @Test
    void mapsAiResponseAndRelatedExperiences() {
        FastApiExplainResponse aiResponse = new FastApiExplainResponse(
                "판소리는 소리꾼과 고수가 함께 만드는 전통 음악입니다.",
                List.of(new FastApiSourceResponse(101L, "판소리", "국가무형유산원", "음악")),
                List.of("판소리", "소리꾼"),
                List.of("음악", "판소리")
        );
        Experience experience = Experience.builder()
                .title("판소리 배우기")
                .locationAddress("서울 종로구")
                .price(BigDecimal.valueOf(30000))
                .durationMinutes(60)
                .tags(List.of("판소리", "음악"))
                .build();

        when(fastApiClient.explainCulture("판소리가 뭐예요?", "ko")).thenReturn(aiResponse);
        when(experienceRepository.findByTagsContainingAny(List.of("판소리", "소리꾼", "음악")))
                .thenReturn(List.of(experience));

        ExplainResponse response = explainService.explain(
                new ExplainRequest("판소리가 뭐예요?", null)
        );

        assertThat(response.getAnswer()).isEqualTo(aiResponse.getAnswer());
        assertThat(response.getSources()).singleElement()
                .extracting(source -> source.getName())
                .isEqualTo("판소리");
        assertThat(response.getRecommendedTags()).containsExactly("판소리", "소리꾼", "음악");
        assertThat(response.getRelatedExperiences()).singleElement()
                .extracting(related -> related.getTitle())
                .isEqualTo("판소리 배우기");
        assertThat(response.isFallback()).isFalse();
        assertThat(response.getMessage()).isNull();
    }

    @Test
    void returnsEmptyRelatedExperiencesWhenAiHasNoMatchingHints() {
        when(fastApiClient.explainCulture("없는 문화재", "en")).thenReturn(
                new FastApiExplainResponse("관련 자료를 찾지 못했습니다.", null, null, null)
        );

        ExplainResponse response = explainService.explain(
                new ExplainRequest("없는 문화재", "en")
        );

        assertThat(response.getSources()).isEmpty();
        assertThat(response.getMatchingKeywords()).isEmpty();
        assertThat(response.getRecommendedCategories()).isEmpty();
        assertThat(response.getRecommendedTags()).isEmpty();
        assertThat(response.getRelatedExperiences()).isEmpty();
        verify(experienceRepository, never()).findByTagsContainingAny(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void propagatesUnavailableErrorWithoutSyntheticFallback() {
        ExternalServiceException unavailable = new ExternalServiceException(
                "AI service is unavailable",
                "AI_UNAVAILABLE"
        );
        when(fastApiClient.explainCulture("질문", "ko")).thenThrow(unavailable);

        assertThatThrownBy(() -> explainService.explain(new ExplainRequest("질문", "ko")))
                .isSameAs(unavailable);
    }

    @Test
    void propagatesTimeoutErrorWithoutSyntheticFallback() {
        ExternalServiceException timeout = new ExternalServiceException(
                "AI response timed out",
                "AI_TIMEOUT"
        );
        when(fastApiClient.explainCulture("질문", "ko")).thenThrow(timeout);

        assertThatThrownBy(() -> explainService.explain(new ExplainRequest("질문", null)))
                .isSameAs(timeout);
    }
}
