package com.trading.user.dto;

import java.math.BigDecimal;

public record UserResponse(
        Long id,
        String username,
        String email,
        String role,
        BigDecimal walletBalance
) {}
