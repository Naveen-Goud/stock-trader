CREATE TABLE stocks (
    id              BIGSERIAL PRIMARY KEY,
    symbol          VARCHAR(10) NOT NULL UNIQUE,
    company_name    VARCHAR(150) NOT NULL,
    sector          VARCHAR(50) NOT NULL,
    current_price   DECIMAL(12,4) NOT NULL,
    previous_close  DECIMAL(12,4) NOT NULL,
    volatility      DECIMAL(12,4) NOT NULL,
    drift           DECIMAL(12,4) NOT NULL,
    market_cap      DECIMAL(20,2),
    last_updated    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_stocks_symbol ON stocks(symbol);
CREATE INDEX idx_stocks_sector ON stocks(sector);

CREATE TABLE price_history (
    id              BIGSERIAL PRIMARY KEY,
    stock_symbol    VARCHAR(10) NOT NULL,
    price           DECIMAL(12,4) NOT NULL,
    change_amount   DECIMAL(12,4) NOT NULL,
    change_percent  DECIMAL(8,4) NOT NULL,
    recorded_at     TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_price_history_symbol_ts ON price_history(stock_symbol, recorded_at DESC);
