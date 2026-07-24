package com.trading.notification.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreatePriceAlertRequest(
        @NotBlank @Pattern(regexp = "^[A-Z]{1,10}$") String symbol,
        @NotBlank @Pattern(regexp = "ABOVE|BELOW") String alertType,
        @NotNull @Positive BigDecimal targetPrice
) {}
