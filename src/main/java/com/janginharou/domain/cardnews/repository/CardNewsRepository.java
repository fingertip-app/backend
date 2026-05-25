package com.janginharou.domain.cardnews.repository;

import com.janginharou.domain.cardnews.entity.CardNews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardNewsRepository extends JpaRepository<CardNews, Long> {

    List<CardNews> findByIsActiveTrue();

    List<CardNews> findByKContentType(String kContentType);

    List<CardNews> findByLinkedExperienceId(Long experienceId);

    List<CardNews> findByPersonalizationTagsContainingAndIsActiveTrue(String tag);
}
