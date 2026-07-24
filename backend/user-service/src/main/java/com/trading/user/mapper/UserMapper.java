package com.trading.user.mapper;

import com.trading.user.dto.UserResponse;
import com.trading.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getWalletBalance()
        );
    }
}
