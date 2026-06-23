package com.janginharou.domain.cardnews.service;

import com.janginharou.domain.cardnews.dto.CardNewsRequest;
import com.janginharou.domain.cardnews.dto.CardNewsResponse;
import com.janginharou.domain.cardnews.entity.CardNews;
import com.janginharou.domain.cardnews.entity.CardNewsExperience;
import com.janginharou.domain.cardnews.repository.CardNewsExperienceRepository;
import com.janginharou.domain.cardnews.repository.CardNewsRepository;
import com.janginharou.domain.experience.entity.Experience;
import com.janginharou.domain.experience.repository.ExperienceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardNewsServiceTest {

    @Mock
    private CardNewsRepository cardNewsRepository;

    @Mock
    private CardNewsExperienceRepository cardNewsExperienceRepository;

    @Mock
    private ExperienceRepository experienceRepository;

    private CardNewsService cardNewsService;

    @BeforeEach
    void setUp() {
        cardNewsService = new CardNewsService(
                cardNewsRepository,
                cardNewsExperienceRepository,
                experienceRepository
        );
    }

    @Test
    void createsCardNewsWithRelatedExperiences() {
        Experience experience = Experience.builder().id(10L).build();
        CardNewsRequest request = CardNewsRequest.builder()
                .title("K-드라마 속 공예")
                .description("문화 설명")
                .kContentType("k_drama")
                .imageUrl("https://example.com/card.png")
                .personalizationTags(Set.of("공예", "전통"))
                .linkedExperienceId(10L)
                .relatedExperienceIds(List.of(10L))
                .build();
        CardNews savedCardNews = CardNews.builder()
                .id(1L)
                .title(request.getTitle())
                .contentType(request.getKContentType())
                .imageUrl(request.getImageUrl())
                .aiExplanation(request.getDescription())
                .categoryTags(List.of("공예", "전통"))
                .isActive(true)
                .build();

        when(cardNewsRepository.save(any(CardNews.class))).thenReturn(savedCardNews);
        when(experienceRepository.findById(10L)).thenReturn(Optional.of(experience));
        when(cardNewsExperienceRepository.findByCardNewsId(1L)).thenReturn(List.of(
                CardNewsExperience.builder()
                        .id(100L)
                        .cardNews(savedCardNews)
                        .experience(experience)
                        .build()
        ));

        CardNewsResponse response = cardNewsService.createCardNews(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getContentType()).isEqualTo("k_drama");
        assertThat(response.getRelatedExperienceIds()).containsExactly(10L);
        verify(cardNewsExperienceRepository).save(any(CardNewsExperience.class));
    }

    @Test
    void deactivatesCardNewsInsteadOfDeletingRow() {
        CardNews cardNews = CardNews.builder()
                .id(1L)
                .title("카드뉴스")
                .contentType("festival")
                .isActive(true)
                .build();

        when(cardNewsRepository.findById(1L)).thenReturn(Optional.of(cardNews));

        cardNewsService.deleteCardNews(1L);

        assertThat(cardNews.getIsActive()).isFalse();
    }
}
