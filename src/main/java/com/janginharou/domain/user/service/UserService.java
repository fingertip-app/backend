package com.janginharou.domain.user.service;

import com.janginharou.domain.user.entity.User;
import com.janginharou.domain.user.repository.UserRepository;
import com.janginharou.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    @Transactional
    public User createUser(User user) {
        // TODO: 회원가입 비즈니스 로직 (중복 검증, 데이터 정규화 등)
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long userId, User updateData) {
        // TODO: 회원 정보 수정 처리
        User user = getUserById(userId);
        return user;
    }

    @Transactional
    public void deleteUser(Long userId) {
        // TODO: 회원 탈퇴 처리 (soft delete 또는 hard delete)
        userRepository.deleteById(userId);
    }
}
