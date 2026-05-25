package com.janginharou.domain.user.repository;

import com.janginharou.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findBySocialIdAndSocialProvider(String socialId, String socialProvider);

    boolean existsByEmail(String email);
}
