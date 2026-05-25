package com.janginharou.domain.cardnews.service;

import com.janginharou.domain.cardnews.entity.CardNews;
import com.janginharou.domain.cardnews.repository.CardNewsRepository;
import com.janginharou.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardNewsService {

    private final CardNewsRepository cardNewsRepository;

    @Transactional(readOnly = true)
    public CardNews getCardNewsById(Long cardNewsId) {
        return cardNewsRepository.findById(cardNewsId)
                .orElseThrow(() -> new ResourceNotFoundException("CardNews", "id", cardNewsId));
    }

    @Transactional(readOnly = true)
    public List<CardNews> getActiveCardNews() {
        return cardNewsRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<CardNews> getCardNewsByKContentType(String kContentType) {
        return cardNewsRepository.findByKContentType(kContentType);
    }

    @Transactional(readOnly = true)
    public List<CardNews> getCardNewsByTag(String tag) {
        return cardNewsRepository.findByPersonalizationTagsContainingAndIsActiveTrue(tag);
    }

    @Transactional
    public CardNews createCardNews(CardNews cardNews) {
        // TODO: 카드뉴스 생성 처리 (이미지 S3 업로드)
        cardNews.setViewCount(0);
        return cardNewsRepository.save(cardNews);
    }

    @Transactional
    public CardNews updateCardNews(Long cardNewsId, CardNews updateData) {
        // TODO: 카드뉴스 수정 처리
        CardNews cardNews = getCardNewsById(cardNewsId);
        return cardNews;
    }

    @Transactional
    public void deleteCardNews(Long cardNewsId) {
        // TODO: 카드뉴스 삭제 처리
        cardNewsRepository.deleteById(cardNewsId);
    }

    @Transactional
    public void incrementViewCount(Long cardNewsId) {
        // TODO: 조회수 증가 처리 (Redis 캐싱 고려)
        CardNews cardNews = getCardNewsById(cardNewsId);
    }
}
