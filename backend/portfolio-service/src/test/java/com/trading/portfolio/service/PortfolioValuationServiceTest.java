package com.trading.portfolio.service;

import com.trading.portfolio.cache.PortfolioSnapshotCache;
import com.trading.portfolio.cache.StockPriceCache;
import com.trading.portfolio.dto.PortfolioSummaryResponse;
import com.trading.portfolio.entity.Holding;
import com.trading.portfolio.repository.HoldingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioValuationServiceTest {

    @Mock private HoldingRepository holdingRepository;
    @Mock private StockPriceCache stockPriceCache;
    @Mock private PortfolioSnapshotCache snapshotCache;

    @InjectMocks
    private PortfolioValuationService service;

    @Test
    void getPortfolioSummary_cacheHit_returnsCachedValue() {
        PortfolioSummaryResponse cached = new PortfolioSummaryResponse(
                BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, List.of());
        when(snapshotCache.get(1L)).thenReturn(cached);

        PortfolioSummaryResponse result = service.getPortfolioSummary(1L);

        assertThat(result).isEqualTo(cached);
        verify(holdingRepository, never()).findByUserId(any());
    }

    @Test
    void getPortfolioSummary_cacheMiss_computesAndCachesFreshSummary() {
        when(snapshotCache.get(1L)).thenReturn(null);

        Holding holding = Holding.builder()
                .id(1L).userId(1L).stockSymbol("AAPL")
                .quantity(10L).avgBuyPrice(new BigDecimal("190.00")).build();
        when(holdingRepository.findByUserId(1L)).thenReturn(List.of(holding));
        when(stockPriceCache.getPrice("AAPL")).thenReturn(new BigDecimal("200.00"));

        PortfolioSummaryResponse result = service.getPortfolioSummary(1L);

        assertThat(result.totalInvested()).isEqualByComparingTo("1900.00");
        assertThat(result.currentValue()).isEqualByComparingTo("2000.00");
        verify(snapshotCache).put(eq(1L), any());
    }
}
