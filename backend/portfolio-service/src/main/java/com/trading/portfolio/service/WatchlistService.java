package com.trading.portfolio.service;

import com.trading.portfolio.cache.StockPriceCache;
import com.trading.portfolio.dto.*;
import com.trading.portfolio.entity.Watchlist;
import com.trading.portfolio.entity.WatchlistItem;
import com.trading.portfolio.exception.DuplicateWatchlistItemException;
import com.trading.portfolio.exception.WatchlistNotFoundException;
import com.trading.portfolio.repository.WatchlistItemRepository;
import com.trading.portfolio.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final WatchlistItemRepository watchlistItemRepository;
    private final StockPriceCache stockPriceCache;

    @Transactional
    public WatchlistResponse createWatchlist(Long userId, CreateWatchlistRequest request) {
        Watchlist watchlist = Watchlist.builder().userId(userId).name(request.name()).build();
        watchlist = watchlistRepository.save(watchlist);
        return toResponse(watchlist);
    }

    public List<WatchlistResponse> getWatchlists(Long userId) {
        return watchlistRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public WatchlistResponse addItem(Long userId, Long watchlistId, AddWatchlistItemRequest request) {
        Watchlist watchlist = watchlistRepository.findByIdAndUserId(watchlistId, userId)
                .orElseThrow(() -> new WatchlistNotFoundException("Watchlist not found: " + watchlistId));

        if (watchlistItemRepository.existsByWatchlistIdAndStockSymbol(watchlistId, request.symbol())) {
            throw new DuplicateWatchlistItemException(
                    String.format("Symbol %s already in watchlist %d", request.symbol(), watchlistId));
        }

        WatchlistItem item = WatchlistItem.builder().watchlist(watchlist).stockSymbol(request.symbol()).build();
        watchlist.getItems().add(item);
        watchlistRepository.save(watchlist);

        return toResponse(watchlist);
    }

    @Transactional
    public void removeItem(Long userId, Long watchlistId, String symbol) {
        Watchlist watchlist = watchlistRepository.findByIdAndUserId(watchlistId, userId)
                .orElseThrow(() -> new WatchlistNotFoundException("Watchlist not found: " + watchlistId));

        watchlist.getItems().removeIf(item -> item.getStockSymbol().equals(symbol));
        watchlistRepository.save(watchlist);
    }

    private WatchlistResponse toResponse(Watchlist watchlist) {
        List<WatchlistItemResponse> items = watchlist.getItems().stream()
                .map(item -> new WatchlistItemResponse(
                        item.getStockSymbol(), stockPriceCache.getPrice(item.getStockSymbol()), item.getAddedAt()))
                .toList();

        return new WatchlistResponse(watchlist.getId(), watchlist.getName(), watchlist.getCreatedAt(), items);
    }
}
