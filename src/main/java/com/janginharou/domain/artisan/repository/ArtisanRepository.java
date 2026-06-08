package com.janginharou.domain.artisan.repository;

import com.janginharou.domain.artisan.entity.Artisan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtisanRepository extends JpaRepository<Artisan, Long> {

    Optional<Artisan> findByUserId(Long userId);

    List<Artisan> findByIsVerifiedTrue();

    List<Artisan> findByHeritageCategory(String heritageCategory);

    boolean existsByUserId(Long userId);
}
