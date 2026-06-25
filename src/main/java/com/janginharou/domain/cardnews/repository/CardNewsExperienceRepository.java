package com.janginharou.domain.cardnews.repository;

import com.janginharou.domain.cardnews.entity.CardNewsExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardNewsExperienceRepository extends JpaRepository<CardNewsExperience, Long> {

    List<CardNewsExperience> findByCardNewsId(Long cardNewsId);

    void deleteByCardNewsId(Long cardNewsId);

    void deleteByExperienceId(Long experienceId);
}
