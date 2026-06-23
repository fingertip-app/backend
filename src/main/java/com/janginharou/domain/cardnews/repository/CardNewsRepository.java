package com.janginharou.domain.cardnews.repository;

import com.janginharou.domain.cardnews.entity.CardNews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardNewsRepository extends JpaRepository<CardNews, Long> {

    List<CardNews> findByIsActiveTrue();

    List<CardNews> findByContentTypeAndIsActiveTrue(String contentType);

    List<CardNews> findByCategoryTagsContainingAndIsActiveTrue(String tag);
}
