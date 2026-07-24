package com.trading.marketdata.service;

import com.trading.marketdata.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceHistoryCleanupService {

    private final PriceHistoryRepository priceHistoryRepository;
    private static final int RETENTION_DAYS = 30;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void purgeOldHistory() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(RETENTION_DAYS);
        priceHistoryRepository.deleteByRecordedAtBefore(cutoff);
        log.info("Purged price history older than {}", cutoff);
    }
}
