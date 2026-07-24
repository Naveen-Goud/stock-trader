package com.trading.marketdata.repository;

import com.trading.marketdata.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findBySymbol(String symbol);

    Page<Stock> findBySector(String sector, Pageable pageable);

    @Query("""
           SELECT s FROM Stock s
           WHERE LOWER(s.symbol) LIKE LOWER(CONCAT('%', :query, '%'))
              OR LOWER(s.companyName) LIKE LOWER(CONCAT('%', :query, '%'))
           """)
    Page<Stock> searchBySymbolOrName(@Param("query") String query, Pageable pageable);

    @Query("SELECT DISTINCT s.sector FROM Stock s ORDER BY s.sector")
    List<String> findAllSectors();
}
