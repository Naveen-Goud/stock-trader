package com.trading.user.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresIn
) {}
