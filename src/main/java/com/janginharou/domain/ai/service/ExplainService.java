package com.janginharou.domain.ai.service;

import com.janginharou.domain.ai.dto.ExplainRequest;
import com.janginharou.domain.ai.dto.ExplainResponse;
import com.janginharou.domain.ai.dto.ExplainSourceResponse;
import com.janginharou.domain.ai.dto.RelatedExperienceResponse;
import com.janginharou.domain.experience.entity.Experience;
import com.janginharou.domain.experience.repository.ExperienceRepository;
import com.janginharou.global.client.FastApiClient;
import com.janginharou.global.client.dto.FastApiExplainResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExplainService {

    private static final String DEFAULT_LOCALE = "ko";
    private static final int MAX_RELATED_EXPERIENCES = 5;

    private final FastApiClient fastApiClient;
    private final ExperienceRepository experienceRepository;

    @Transactional(readOnly = true)
    public ExplainResponse explain(ExplainRequest request) {
        FastApiExplainResponse aiResponse = fastApiClient.explainCulture(
                request.getQuery(),
                normalizeLocale(request.getLocale())
        );

        List<String> matchingKeywords = safeList(aiResponse.getMatchingKeywords());
        List<String> recommendedCategories = safeList(aiResponse.getRecommendedCategories());
        List<String> recommendedTags = mergeTags(matchingKeywords, recommendedCategories);
        List<Experience> relatedExperiences = recommendedTags.isEmpty()
                ? List.of()
                : experienceRepository.findByTagsContainingAny(recommendedTags);

        return ExplainResponse.builder()
                .answer(aiResponse.getAnswer())
                .sources(safeList(aiResponse.getSources()).stream()
                        .map(ExplainSourceResponse::from)
                        .toList())
                .matchingKeywords(matchingKeywords)
                .recommendedCategories(recommendedCategories)
                .recommendedTags(recommendedTags)
                .relatedExperiences(relatedExperiences.stream()
                        .limit(MAX_RELATED_EXPERIENCES)
                        .map(RelatedExperienceResponse::from)
                        .toList())
                .fallback(false)
                .message(null)
                .build();
    }

    private String normalizeLocale(String locale) {
        return locale == null || locale.isBlank() ? DEFAULT_LOCALE : locale;
    }

    private List<String> mergeTags(List<String> matchingKeywords, List<String> recommendedCategories) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        tags.addAll(matchingKeywords);
        tags.addAll(recommendedCategories);
        return new ArrayList<>(tags);
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
}
