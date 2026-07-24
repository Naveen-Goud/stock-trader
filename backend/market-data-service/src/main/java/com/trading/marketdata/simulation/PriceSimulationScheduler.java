package com.trading.marketdata.simulation;

import com.trading.marketdata.entity.Stock;
import com.trading.marketdata.repository.StockRepository;
import com.trading.marketdata.service.PriceUpdateService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class PriceSimulationScheduler {

    private final StockRepository stockRepository;
    private final PriceUpdateService priceUpdateService;
    private final Map<String, GeometricBrownianMotion> simulators = new ConcurrentHashMap<>();

    @PostConstruct
    public void initSimulators() {
        List<Stock> stocks = stockRepository.findAll();
        stocks.forEach(stock ->
                simulators.put(stock.getSymbol(), new GeometricBrownianMotion(stock.getCurrentPrice())));
        log.info("Initialized GBM simulators for {} stocks", stocks.size());
    }

    @Scheduled(fixedRate = 2000)
    public void simulatePrices() {
        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {
            GeometricBrownianMotion gbm = simulators.computeIfAbsent(
                    stock.getSymbol(), sym -> new GeometricBrownianMotion(stock.getCurrentPrice()));

            BigDecimal nextPrice = gbm.nextPrice(
                    stock.getCurrentPrice(), stock.getDrift().doubleValue(), stock.getVolatility().doubleValue());
            nextPrice = gbm.applyMarketShock(nextPrice);

            priceUpdateService.applyPriceUpdate(stock, nextPrice);
        }
    }

    @Scheduled(cron = "0 0 16 * * MON-FRI", zone = "America/New_York")
    public void resetDailyClose() {
        List<Stock> stocks = stockRepository.findAll();
        stocks.forEach(stock -> {
            stock.setPreviousClose(stock.getCurrentPrice());
            stockRepository.save(stock);
        });
    }
}
