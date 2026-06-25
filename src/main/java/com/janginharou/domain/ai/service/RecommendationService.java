package com.janginharou.domain.ai.service;

import com.janginharou.domain.ai.dto.AiRecommendationRequest;
import com.janginharou.domain.ai.dto.AiRecommendationResponse;
import com.janginharou.domain.ai.dto.RecommendedExperienceResponse;
import com.janginharou.domain.experience.entity.Experience;
import com.janginharou.domain.experience.repository.ExperienceRepository;
import com.janginharou.global.client.FastApiClient;
import com.janginharou.global.client.dto.FastApiRecommendationRequest;
import com.janginharou.global.client.dto.FastApiRecommendationResponse;
import com.janginharou.global.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final String DEFAULT_LOCALE = "ko";
    private static final int MAX_RECOMMENDED_EXPERIENCES = 5;
    private static final String FALLBACK_MESSAGE = "AI 기능이 일시적으로 불가능합니다. 기본 추천 체험을 표시합니다.";

    private final FastApiClient fastApiClient;
    private final ExperienceRepository experienceRepository;

    @Transactional(readOnly = true)
    public AiRecommendationResponse recommend(AiRecommendationRequest request) {
        try {
            // Python AI 서버 호출 - Experience ID만 받음
            FastApiRecommendationRequest fastApiRequest = buildFastApiRequest(request);
            FastApiRecommendationResponse aiResponse = fastApiClient.getRecommendations(fastApiRequest);

            // Experience ID로 DB에서 전체 데이터 + 이미지 조회
            List<RecommendedExperienceResponse> recommendations = fetchExperiencesByIds(
                    aiResponse.getRecommendedExperienceIds(),
                    aiResponse.getReasons()
            );

            return AiRecommendationResponse.builder()
                    .answer(aiResponse.getAnswer())
                    .sources(List.of())
                    .matchingKeywords(safeList(aiResponse.getMatchingKeywords()))
                    .recommendedTags(safeList(aiResponse.getMatchingKeywords()))
                    .recommendedExperiences(recommendations)
                    .fallback(false)
                    .message(null)
                    .build();
        } catch (ExternalServiceException e) {
            if (isRetryableError(e)) {
                return handleFallback(request);
            }
            throw e;
        }
    }

    private FastApiRecommendationRequest buildFastApiRequest(AiRecommendationRequest request) {
        List<FastApiRecommendationRequest.ConversationMessage> history =
            request.getConversationHistory() != null
                ? request.getConversationHistory().stream()
                    .map(msg -> new FastApiRecommendationRequest.ConversationMessage(
                        msg.getRole(),
                        msg.getContent()
                    ))
                    .toList()
                : List.of();

        return new FastApiRecommendationRequest(
                request.getFreeText(),
                request.getCompanionType().name(),
                request.getHeadCount(),
                request.getInterests(),
                request.getRegion(),
                request.getTimePreference() != null ? request.getTimePreference().name() : "ANYTIME",
                history,
                normalizeLocale(request.getLocale())
        );
    }

    private List<RecommendedExperienceResponse> fetchExperiencesByIds(
            List<Long> experienceIds,
            java.util.Map<String, String> reasons
    ) {
        if (experienceIds == null || experienceIds.isEmpty()) {
            return List.of();
        }

        return experienceRepository.findAllById(experienceIds).stream()
                .map(exp -> {
                    String reason = reasons != null ? reasons.get(String.valueOf(exp.getId())) : null;
                    return RecommendedExperienceResponse.from(
                            exp,
                            reason != null ? reason : "AI가 추천한 체험입니다"
                    );
                })
                .toList();
    }


    private AiRecommendationResponse handleFallback(AiRecommendationRequest request) {
        // Find active experiences as fallback
        List<Experience> fallbackExperiences = experienceRepository.findByIsActiveTrue();

        if (fallbackExperiences.isEmpty()) {
            throw new ExternalServiceException(
                    "AI service unavailable and no fallback experiences available",
                    "AI_UNAVAILABLE"
            );
        }

        List<RecommendedExperienceResponse> recommendations = fallbackExperiences.stream()
                .filter(e -> e.getMaxParticipants() >= request.getHeadCount())
                .sorted((left, right) -> left.getId().compareTo(right.getId()))
                .limit(MAX_RECOMMENDED_EXPERIENCES)
                .map(exp -> RecommendedExperienceResponse.from(exp, "기본 활성 체험으로 추천"))
                .toList();

        if (recommendations.isEmpty()) {
            throw new ExternalServiceException(
                    "AI service unavailable and no fallback experiences match the requested head count",
                    "AI_UNAVAILABLE"
            );
        }

        return AiRecommendationResponse.builder()
                .answer("현재 AI 기능을 이용할 수 없어 기본 추천 체험을 제공합니다.")
                .sources(List.of())
                .matchingKeywords(List.of())
                .recommendedTags(List.of())
                .recommendedExperiences(recommendations)
                .fallback(true)
                .message(FALLBACK_MESSAGE)
                .build();
    }


    private boolean isRetryableError(ExternalServiceException e) {
        String errorCode = e.getErrorCode();
        return "AI_UNAVAILABLE".equals(errorCode) || "AI_TIMEOUT".equals(errorCode);
    }


    private String normalizeLocale(String locale) {
        return locale == null || locale.isBlank() ? DEFAULT_LOCALE : locale;
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

}
