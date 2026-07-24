package com.trading.user.service;

import com.trading.user.dto.UserResponse;
import com.trading.user.exception.InvalidCredentialsException;
import com.trading.user.mapper.UserMapper;
import com.trading.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Cacheable(value = "userProfile", key = "#userId")
    public UserResponse getProfile(Long userId) {
        return userMapper.toResponse(
                userRepository.findById(userId)
                        .orElseThrow(() -> new InvalidCredentialsException("User not found"))
        );
    }
}
