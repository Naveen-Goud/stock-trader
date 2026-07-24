package com.trading.marketdata.simulation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

/**
 * Geometric Brownian Motion price simulator.
 *
 * Formula: S(t+dt) = S(t) * exp((mu - sigma^2/2)*dt + sigma*sqrt(dt)*Z)
 *
 * mu = drift, sigma = volatility, dt = time step, Z = standard normal random variable.
 * Produces log-normally distributed returns -- prices stay positive.
 *
 * Price floor at $0.01 prevents degenerate prices.
 * Price ceiling at 10x initial price prevents runaway simulation.
 */
public class GeometricBrownianMotion {

    private static final Random RANDOM = new Random();
    private static final double DT = 1.0 / (252 * 6.5 * 3600);
    private static final BigDecimal PRICE_FLOOR = new BigDecimal("0.01");
    private static final double PRICE_CEILING_MULTIPLIER = 10.0;

    private final double initialPrice;

    public GeometricBrownianMotion(BigDecimal initialPrice) {
        this.initialPrice = initialPrice.doubleValue();
    }

    public BigDecimal nextPrice(BigDecimal currentPrice, double mu, double sigma) {
        double S = currentPrice.doubleValue();
        double Z = RANDOM.nextGaussian();

        double exponent = (mu - 0.5 * sigma * sigma) * DT + sigma * Math.sqrt(DT) * Z;
        double nextS = S * Math.exp(exponent);

        nextS = Math.max(nextS, PRICE_FLOOR.doubleValue());
        nextS = Math.min(nextS, initialPrice * PRICE_CEILING_MULTIPLIER);

        return BigDecimal.valueOf(nextS).setScale(4, RoundingMode.HALF_UP);
    }

    public BigDecimal applyMarketShock(BigDecimal currentPrice) {
        if (RANDOM.nextDouble() < 0.001) {
            double shockMagnitude = 0.03 + RANDOM.nextDouble() * 0.05;
            double direction = RANDOM.nextBoolean() ? 1.0 : -1.0;
            double shocked = currentPrice.doubleValue() * (1 + direction * shockMagnitude);
            shocked = Math.max(shocked, PRICE_FLOOR.doubleValue());
            return BigDecimal.valueOf(shocked).setScale(4, RoundingMode.HALF_UP);
        }
        return currentPrice;
    }
}
