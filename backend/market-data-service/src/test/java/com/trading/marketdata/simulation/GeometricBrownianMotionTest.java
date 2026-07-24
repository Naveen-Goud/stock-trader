package com.trading.marketdata.simulation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class GeometricBrownianMotionTest {

    private GeometricBrownianMotion gbm;
    private static final BigDecimal BASE_PRICE = new BigDecimal("200.00");

    @BeforeEach
    void setUp() {
        gbm = new GeometricBrownianMotion(BASE_PRICE);
    }

    @RepeatedTest(100)
    void nextPrice_alwaysPositive() {
        BigDecimal next = gbm.nextPrice(BASE_PRICE, 0.00005, 0.018);
        assertThat(next).isGreaterThan(BigDecimal.ZERO);
    }

    @RepeatedTest(50)
    void nextPrice_staysWithinCeiling() {
        BigDecimal current = BASE_PRICE;
        for (int i = 0; i < 500; i++) {
            current = gbm.nextPrice(current, 0.0001, 0.05);
        }
        assertThat(current).isLessThanOrEqualTo(new BigDecimal("2000.00"));
    }

    @Test
    void nextPrice_scaleIsFourDecimalPlaces() {
        BigDecimal next = gbm.nextPrice(BASE_PRICE, 0.00005, 0.018);
        assertThat(next.scale()).isEqualTo(4);
    }

    @RepeatedTest(20)
    void nextPrice_floorEnforcedForLowPriceStock() {
        BigDecimal nearZero = new BigDecimal("0.02");
        BigDecimal next = gbm.nextPrice(nearZero, -0.0001, 0.05);
        assertThat(next).isGreaterThanOrEqualTo(new BigDecimal("0.01"));
    }
}
