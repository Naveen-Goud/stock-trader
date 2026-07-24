CREATE TABLE holdings (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    stock_symbol    VARCHAR(10) NOT NULL,
    quantity        BIGINT NOT NULL CHECK (quantity >= 0),
    avg_buy_price   DECIMAL(12,4) NOT NULL,
    updated_at      TIMESTAMP NOT NULL DEFAULT now(),
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_user_stock UNIQUE (user_id, stock_symbol)
);

CREATE INDEX idx_holdings_user_id ON holdings(user_id);

CREATE TABLE watchlists (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    name        VARCHAR(100) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_watchlists_user_id ON watchlists(user_id);

CREATE TABLE watchlist_items (
    id              BIGSERIAL PRIMARY KEY,
    watchlist_id    BIGINT NOT NULL REFERENCES watchlists(id) ON DELETE CASCADE,
    stock_symbol    VARCHAR(10) NOT NULL,
    added_at        TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_watchlist_stock UNIQUE (watchlist_id, stock_symbol)
);

CREATE INDEX idx_watchlist_items_watchlist_id ON watchlist_items(watchlist_id);
