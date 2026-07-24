package com.trading.trading.dto;

import jakarta.validation.constraints.*;

public record TradeRequest(
        @NotBlank @Pattern(regexp = "^[A-Z]{1,10}$", message = "Symbol must be 1-10 uppercase letters")
        String symbol,
        @NotNull @Positive(message = "Quantity must be positive")
        Long quantity
) {}
