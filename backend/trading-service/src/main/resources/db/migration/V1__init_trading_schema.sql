CREATE TABLE trades (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    stock_symbol    VARCHAR(10) NOT NULL,
    trade_type      VARCHAR(4) NOT NULL CHECK (trade_type IN ('BUY','SELL')),
    quantity        BIGINT NOT NULL CHECK (quantity > 0),
    price           DECIMAL(12,4) NOT NULL,
    total_amount    DECIMAL(15,2) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'EXECUTED',
    idempotency_key VARCHAR(100) UNIQUE,
    executed_at     TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_trades_user_id ON trades(user_id);
CREATE INDEX idx_trades_user_executed ON trades(user_id, executed_at DESC);

CREATE TABLE outbox_events (
    id              BIGSERIAL PRIMARY KEY,
    aggregate_id    BIGINT NOT NULL,
    event_type      VARCHAR(50) NOT NULL,
    topic           VARCHAR(100) NOT NULL,
    partition_key   VARCHAR(50) NOT NULL,
    payload         TEXT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    retry_count     INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_outbox_status ON outbox_events(status);
