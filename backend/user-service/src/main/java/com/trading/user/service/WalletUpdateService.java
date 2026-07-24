package com.trading.user.service;

import com.trading.common.event.TradeExecutedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.trading.user.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletUpdateService {

    private final UserRepository userRepository;

    @Transactional
    @CacheEvict(value = "userProfile", key = "#event.userId()")
    public void applyTrade(TradeExecutedEvent event) {
        if ("BUY".equals(event.tradeType())) {
            int updated = userRepository.debitWallet(event.userId(), event.totalAmount());
            if (updated == 0) {
                // Should be extremely rare: trading-service already checked the
                // balance before executing the trade. Logged loudly rather than
                // thrown, since the trade itself already happened and cannot be
                // silently rolled back from here.
                log.error("Wallet debit failed for user {} (trade {}): insufficient balance at settlement time",
                        event.userId(), event.tradeId());
            }
        } else {
            userRepository.creditWallet(event.userId(), event.totalAmount());
        }
    }
}
