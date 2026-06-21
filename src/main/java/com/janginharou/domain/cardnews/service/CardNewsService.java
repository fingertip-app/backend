package com.janginharou.domain.cardnews.service;

import com.janginharou.domain.cardnews.dto.CardNewsRequest;
import com.janginharou.domain.cardnews.dto.CardNewsResponse;
import com.janginharou.domain.cardnews.entity.CardNews;
import com.janginharou.domain.cardnews.entity.CardNewsExperience;
import com.janginharou.domain.cardnews.repository.CardNewsExperienceRepository;
import com.janginharou.domain.cardnews.repository.CardNewsRepository;
import com.janginharou.domain.experience.entity.Experience;
import com.janginharou.domain.experience.repository.ExperienceRepository;
import com.janginharou.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CardNewsService {

    private final CardNewsRepository cardNewsRepository;
    private final CardNewsExperienceRepository cardNewsExperienceRepository;
    private final ExperienceRepository experienceRepository;

    @Transactional(readOnly = true)
    public CardNews getCardNewsById(Long cardNewsId) {
        return cardNewsRepository.findById(cardNewsId)
                .orElseThrow(() -> new ResourceNotFoundException("CardNews", "id", cardNewsId));
    }

    @Transactional(readOnly = true)
    public CardNewsResponse getCardNewsResponseById(Long cardNewsId) {
        return toResponse(getCardNewsById(cardNewsId));
    }

    @Transactional(readOnly = true)
    public List<CardNewsResponse> getActiveCardNews() {
        return cardNewsRepository.findByIsActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CardNewsResponse> getCardNewsByContentType(String contentType) {
        return cardNewsRepository.findByContentTypeAndIsActiveTrue(contentType)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CardNewsResponse> getCardNewsByTag(String tag) {
        return cardNewsRepository.findByCategoryTagsContainingAndIsActiveTrue(tag)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CardNewsResponse createCardNews(CardNewsRequest request) {
        CardNews cardNews = CardNews.builder()
                .title(request.getTitle())
                .contentType(request.getKContentType())
                .imageUrl(request.getImageUrl())
                .aiExplanation(request.getDescription())
                .categoryTags(resolveTags(request))
                .isActive(true)
                .build();

        CardNews savedCardNews = cardNewsRepository.save(cardNews);
        replaceRelatedExperiences(savedCardNews, resolveRelatedExperienceIds(request));
        return toResponse(savedCardNews);
    }

    @Transactional
    public CardNewsResponse updateCardNews(Long cardNewsId, CardNewsRequest request) {
        CardNews cardNews = getCardNewsById(cardNewsId);
        cardNews.update(
                request.getTitle(),
                request.getKContentType(),
                request.getImageUrl(),
                request.getDescription(),
                resolveTags(request)
        );
        replaceRelatedExperiences(cardNews, resolveRelatedExperienceIds(request));
        return toResponse(cardNews);
    }

    @Transactional
    public void deleteCardNews(Long cardNewsId) {
        CardNews cardNews = getCardNewsById(cardNewsId);
        cardNews.deactivate();
    }

    private CardNewsResponse toResponse(CardNews cardNews) {
        List<Long> relatedExperienceIds = cardNewsExperienceRepository.findByCardNewsId(cardNews.getId())
                .stream()
                .map(cardNewsExperience -> cardNewsExperience.getExperience().getId())
                .toList();
        return CardNewsResponse.from(cardNews, relatedExperienceIds);
    }

    private void replaceRelatedExperiences(CardNews cardNews, List<Long> relatedExperienceIds) {
        cardNewsExperienceRepository.deleteByCardNewsId(cardNews.getId());
        relatedExperienceIds.stream()
                .map(experienceId -> experienceRepository.findById(experienceId)
                        .orElseThrow(() -> new ResourceNotFoundException("Experience", "id", experienceId)))
                .map(experience -> toCardNewsExperience(cardNews, experience))
                .forEach(cardNewsExperienceRepository::save);
    }

    private CardNewsExperience toCardNewsExperience(CardNews cardNews, Experience experience) {
        return CardNewsExperience.builder()
                .cardNews(cardNews)
                .experience(experience)
                .build();
    }

    private List<Long> resolveRelatedExperienceIds(CardNewsRequest request) {
        Set<Long> ids = new LinkedHashSet<>();
        if (request.getLinkedExperienceId() != null) {
            ids.add(request.getLinkedExperienceId());
        }
        if (request.getRelatedExperienceIds() != null) {
            ids.addAll(request.getRelatedExperienceIds());
        }
        return new ArrayList<>(ids);
    }

    private List<String> resolveTags(CardNewsRequest request) {
        if (request.getPersonalizationTags() == null || request.getPersonalizationTags().isEmpty()) {
            return List.of();
        }
        return request.getPersonalizationTags()
                .stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }
}
