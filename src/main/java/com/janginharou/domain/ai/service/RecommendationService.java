package com.janginharou.domain.ai.service;

import com.janginharou.domain.ai.dto.AiRecommendationRequest;
import com.janginharou.domain.ai.dto.AiRecommendationRequest.CompanionType;
import com.janginharou.domain.ai.dto.AiRecommendationRequest.ConversationMessage;
import com.janginharou.domain.ai.dto.AiRecommendationRequest.TimePreference;
import com.janginharou.domain.ai.dto.AiRecommendationResponse;
import com.janginharou.domain.ai.dto.ExplainSourceResponse;
import com.janginharou.domain.ai.dto.RecommendedExperienceResponse;
import com.janginharou.domain.experience.entity.Experience;
import com.janginharou.domain.experience.repository.ExperienceRepository;
import com.janginharou.global.client.FastApiClient;
import com.janginharou.global.client.dto.FastApiExplainResponse;
import com.janginharou.global.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
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
        String query = buildQuery(request);

        try {
            FastApiExplainResponse aiResponse = fastApiClient.explainCulture(
                    query,
                    normalizeLocale(request.getLocale())
            );

            List<String> matchingKeywords = safeList(aiResponse.getMatchingKeywords());
            List<String> recommendedCategories = safeList(aiResponse.getRecommendedCategories());
            List<String> recommendedTags = mergeTags(
                    request.getInterests(),
                    matchingKeywords,
                    recommendedCategories
            );

            List<RecommendedExperienceResponse> recommendations = findRecommendedExperiences(
                    request,
                    recommendedTags,
                    false
            );

            return AiRecommendationResponse.builder()
                    .answer(aiResponse.getAnswer())
                    .sources(safeList(aiResponse.getSources()).stream()
                            .map(ExplainSourceResponse::from)
                            .toList())
                    .matchingKeywords(matchingKeywords)
                    .recommendedTags(recommendedTags)
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

    private String buildQuery(AiRecommendationRequest request) {
        StringBuilder sb = new StringBuilder();

        // companionType + headCount
        sb.append(formatCompanionType(request.getCompanionType()))
                .append(" ")
                .append(request.getHeadCount())
                .append("명");

        // region (optional)
        if (isNotBlank(request.getRegion())) {
            sb.append("이 ")
                    .append(request.getRegion())
                    .append("에서");
        } else {
            sb.append("이");
        }

        // timePreference (optional)
        if (request.getTimePreference() != null) {
            sb.append(" ")
                    .append(formatTimePreference(request.getTimePreference()))
                    .append("에");
        }

        sb.append(" 할 수 있는 체험을 추천해줘.\n");

        // interests
        sb.append("관심사는 ")
                .append(String.join(", ", request.getInterests()))
                .append("이야.\n");

        // freeText (optional)
        if (isNotBlank(request.getFreeText())) {
            sb.append("추가 요청: ")
                    .append(request.getFreeText())
                    .append("\n");
        }

        // conversationHistory (last 2 messages, optional)
        List<ConversationMessage> history = request.getConversationHistory();
        if (history != null && !history.isEmpty()) {
            sb.append("이전 대화:\n");
            int startIndex = Math.max(0, history.size() - 2);
            for (int i = startIndex; i < history.size(); i++) {
                ConversationMessage msg = history.get(i);
                sb.append(msg.getRole())
                        .append(": ")
                        .append(msg.getContent())
                        .append("\n");
            }
        }

        return sb.toString();
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

    private List<RecommendedExperienceResponse> findRecommendedExperiences(
            AiRecommendationRequest request,
            List<String> tagPool,
            boolean isFallback
    ) {
        if (tagPool.isEmpty()) {
            return List.of();
        }

        List<Experience> candidates = experienceRepository.findByTagsContainingAny(tagPool);

        String matchReason = isFallback ? "기본 활성 체험으로 추천" : "태그와 매칭";

        return candidates.stream()
                .filter(e -> isRegionMatching(e, request.getRegion()))
                .filter(e -> e.getMaxParticipants() >= request.getHeadCount())
                .limit(MAX_RECOMMENDED_EXPERIENCES)
                .map(exp -> RecommendedExperienceResponse.from(exp, matchReason))
                .toList();
    }

    private boolean isRegionMatching(Experience experience, String region) {
        if (!isNotBlank(region)) {
            return true;
        }
        String locationAddress = experience.getLocationAddress();
        return locationAddress != null && locationAddress.contains(region);
    }

    private boolean isRetryableError(ExternalServiceException e) {
        String errorCode = e.getErrorCode();
        return "AI_UNAVAILABLE".equals(errorCode) || "AI_TIMEOUT".equals(errorCode);
    }

    private List<String> mergeTags(List<String> interests, List<String> matchingKeywords, List<String> recommendedCategories) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        tags.addAll(interests);
        tags.addAll(matchingKeywords);
        tags.addAll(recommendedCategories);
        return new ArrayList<>(tags);
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

    private String formatCompanionType(CompanionType type) {
        return switch (type) {
            case ALONE -> "혼자";
            case FRIEND -> "친구";
            case FAMILY -> "가족";
            case COUPLE -> "연인";
            case KIDS -> "아이";
            case FOREIGN_GUEST -> "외국인 손님";
            case OTHER -> "기타";
        };
    }

    private String formatTimePreference(TimePreference preference) {
        return switch (preference) {
            case MORNING -> "아침";
            case AFTERNOON -> "오후";
            case EVENING -> "저녁";
            case WEEKDAY -> "평일";
            case WEEKEND -> "주말";
            case ANYTIME -> "언제든";
        };
    }
}
