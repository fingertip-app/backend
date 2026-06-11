package com.janginharou.domain.ai.controller;

import com.janginharou.domain.ai.dto.AiRecommendationResponse;
import com.janginharou.domain.ai.dto.RecommendedExperienceResponse;
import com.janginharou.domain.ai.service.RecommendationService;
import com.janginharou.global.config.SecurityConfig;
import com.janginharou.global.exception.ExternalServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecommendationController.class)
@WithMockUser
@Import(SecurityConfig.class)
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecommendationService recommendationService;

    @Test
    void shouldReturnRecommendationsWithStatus200() throws Exception {
        // Arrange
        RecommendedExperienceResponse experience = RecommendedExperienceResponse.builder()
                .id(1L)
                .title("전통 매듭 만들기")
                .location("서울 종로구")
                .price(BigDecimal.valueOf(30000))
                .durationMinutes(60)
                .tags(List.of("공예", "매듭"))
                .matchReason("태그와 매칭")
                .build();

        AiRecommendationResponse response = AiRecommendationResponse.builder()
                .answer("친구와 함께 공예 체험을 추천합니다.")
                .sources(List.of())
                .matchingKeywords(List.of("공예"))
                .recommendedTags(List.of("공예", "매듭"))
                .recommendedExperiences(List.of(experience))
                .fallback(false)
                .message(null)
                .build();

        when(recommendationService.recommend(any())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/v1/ai/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companionType": "FRIEND",
                                  "headCount": 2,
                                  "interests": ["공예", "매듭"],
                                  "locale": "ko"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.answer").value("친구와 함께 공예 체험을 추천합니다."))
                .andExpect(jsonPath("$.data.recommendedExperiences[0].title").value("전통 매듭 만들기"))
                .andExpect(jsonPath("$.data.fallback").value(false));
    }

    @Test
    void shouldRejectMissingCompanionType() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/v1/ai/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "headCount": 2,
                                  "interests": ["공예"]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    @Test
    void shouldRejectHeadCountOutOfRange() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/v1/ai/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companionType": "FRIEND",
                                  "headCount": 25,
                                  "interests": ["공예"]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    @Test
    void shouldReturnFallbackResponseWhenAiFails() throws Exception {
        // Arrange
        RecommendedExperienceResponse fallbackExp = RecommendedExperienceResponse.builder()
                .id(1L)
                .title("기본 체험")
                .location("서울")
                .price(BigDecimal.valueOf(30000))
                .durationMinutes(60)
                .tags(List.of("공예"))
                .matchReason("기본 활성 체험으로 추천")
                .build();

        AiRecommendationResponse fallbackResponse = AiRecommendationResponse.builder()
                .answer("현재 AI 기능을 이용할 수 없어 기본 추천 체험을 제공합니다.")
                .sources(List.of())
                .matchingKeywords(List.of())
                .recommendedTags(List.of())
                .recommendedExperiences(List.of(fallbackExp))
                .fallback(true)
                .message("AI 기능이 일시적으로 불가능합니다. 기본 추천 체험을 표시합니다.")
                .build();

        when(recommendationService.recommend(any())).thenReturn(fallbackResponse);

        // Act & Assert
        mockMvc.perform(post("/v1/ai/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companionType": "FRIEND",
                                  "headCount": 2,
                                  "interests": ["공예"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fallback").value(true))
                .andExpect(jsonPath("$.data.message").value("AI 기능이 일시적으로 불가능합니다. 기본 추천 체험을 표시합니다."));
    }

    @Test
    void shouldPropagateConfigErrorWithStatus503() throws Exception {
        // Arrange
        when(recommendationService.recommend(any())).thenThrow(
                new ExternalServiceException("AI config error", "AI_CONFIG_ERROR")
        );

        // Act & Assert
        mockMvc.perform(post("/v1/ai/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companionType": "FRIEND",
                                  "headCount": 2,
                                  "interests": ["공예"]
                                }
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AI_CONFIG_ERROR"));
    }
}
