package com.trading.trading.dto;

import java.math.BigDecimal;

public record WalletBalanceResponse(Long userId, BigDecimal walletBalance) {}
