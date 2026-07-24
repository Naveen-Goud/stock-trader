package com.trading.marketdata.service;

import com.trading.marketdata.cache.StockPriceWriteThroughCache;
import com.trading.marketdata.dto.*;
import com.trading.marketdata.entity.Stock;
import com.trading.marketdata.exception.StockNotFoundException;
import com.trading.marketdata.mapper.StockMapper;
import com.trading.marketdata.repository.PriceHistoryRepository;
import com.trading.marketdata.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockMarketService {

    private final StockRepository stockRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final StockPriceWriteThroughCache priceCache;
    private final StockMapper stockMapper;

    private static final int PRICE_HISTORY_LIMIT = 100;

    public StockPageResponse getStocks(int page, int size, String sector, String sortBy) {
        Sort sort = resolveSort(sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Stock> stockPage = sector != null && !sector.isBlank()
                ? stockRepository.findBySector(sector, pageable)
                : stockRepository.findAll(pageable);

        List<StockResponse> responses = stockPage.getContent().stream()
                .map(stockMapper::toResponse).toList();

        return new StockPageResponse(responses, stockPage.getTotalElements(), stockPage.getTotalPages(), page, size);
    }

    public StockDetailResponse getStockBySymbol(String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new StockNotFoundException("Stock not found: " + symbol));

        BigDecimal livePrice = priceCache.getPrice(symbol.toUpperCase());
        if (livePrice != null && livePrice.compareTo(BigDecimal.ZERO) > 0) {
            stock.setCurrentPrice(livePrice);
        }

        var history = priceHistoryRepository.findByStockSymbolOrderByRecordedAtDesc(
                symbol.toUpperCase(), PageRequest.of(0, PRICE_HISTORY_LIMIT));

        return stockMapper.toDetailResponse(stock, history);
    }

    public StockPageResponse searchStocks(String query, int page, int size) {
        if (query == null || query.isBlank()) {
            return getStocks(page, size, null, null);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Stock> results = stockRepository.searchBySymbolOrName(query, pageable);

        return new StockPageResponse(
                results.getContent().stream().map(stockMapper::toResponse).toList(),
                results.getTotalElements(), results.getTotalPages(), page, size);
    }

    public List<String> getSectors() {
        return stockRepository.findAllSectors();
    }

    private Sort resolveSort(String sortBy) {
        if (sortBy == null) return Sort.by("symbol").ascending();
        return switch (sortBy) {
            case "price_asc"   -> Sort.by("currentPrice").ascending();
            case "price_desc"  -> Sort.by("currentPrice").descending();
            case "name"        -> Sort.by("companyName").ascending();
            case "sector"      -> Sort.by("sector").ascending();
            default            -> Sort.by("symbol").ascending();
        };
    }
}
