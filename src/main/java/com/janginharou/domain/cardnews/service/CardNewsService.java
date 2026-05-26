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
    public List<CardNews> getCardNewsByContentType(String contentType) {
        return cardNewsRepository.findByKContentType(contentType);
    }

    @Transactional(readOnly = true)
    public List<CardNews> getCardNewsByTag(String tag) {
        return cardNewsRepository.findByCategoryTagsContainingAndIsActiveTrue(tag);
    }

    @Transactional
    public CardNews createCardNews(CardNews cardNews) {
        // TODO: 카드뉴스 생성 처리 (이미지 S3 업로드)
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
        cardNewsRepository.deleteById(cardNewsId);
    }
}
