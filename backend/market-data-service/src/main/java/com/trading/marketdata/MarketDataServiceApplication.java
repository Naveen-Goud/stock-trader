package com.trading.marketdata;

import com.trading.marketdata.cache.StockPriceWriteThroughCache;
import com.trading.marketdata.repository.StockRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class MarketDataServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketDataServiceApplication.class, args);
    }

    @Bean
    public ApplicationRunner warmUpCache(StockRepository stockRepository,
                                          StockPriceWriteThroughCache cache) {
        return args -> {
            var stocks = stockRepository.findAll();
            cache.warmUpCache(stocks);
        };
    }
}
