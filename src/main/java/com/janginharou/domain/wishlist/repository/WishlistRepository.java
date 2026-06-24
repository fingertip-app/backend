package com.janginharou.domain.wishlist.repository;

import com.janginharou.domain.wishlist.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    /**
     * 사용자 ID로 위시리스트 조회
     */
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId ORDER BY w.createdAt DESC")
    List<Wishlist> findByUserId(@Param("userId") Long userId);

    /**
     * 사용자 ID와 체험 ID로 위시리스트 조회
     */
    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId AND w.experience.id = :experienceId")
    Optional<Wishlist> findByUserIdAndExperienceId(
            @Param("userId") Long userId,
            @Param("experienceId") Long experienceId
    );

    /**
     * 사용자 ID와 체험 ID로 존재 여부 확인
     */
    @Query("SELECT COUNT(w) > 0 FROM Wishlist w WHERE w.user.id = :userId AND w.experience.id = :experienceId")
    boolean existsByUserIdAndExperienceId(
            @Param("userId") Long userId,
            @Param("experienceId") Long experienceId
    );

    /**
     * 체험 ID로 위시리스트 개수 조회
     */
    @Query("SELECT COUNT(w) FROM Wishlist w WHERE w.experience.id = :experienceId")
    long countByExperienceId(@Param("experienceId") Long experienceId);

    /**
     * 사용자 ID로 위시리스트 개수 조회
     */
    @Query("SELECT COUNT(w) FROM Wishlist w WHERE w.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
}
