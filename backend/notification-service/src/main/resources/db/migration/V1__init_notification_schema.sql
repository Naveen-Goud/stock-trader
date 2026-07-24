CREATE TABLE notifications (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    type            VARCHAR(30) NOT NULL,
    message         VARCHAR(500) NOT NULL,
    related_symbol  VARCHAR(10),
    is_read         BOOLEAN NOT NULL DEFAULT false,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

CREATE TABLE price_alerts (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    stock_symbol    VARCHAR(10) NOT NULL,
    alert_type      VARCHAR(10) NOT NULL CHECK (alert_type IN ('ABOVE','BELOW')),
    target_price    DECIMAL(12,4) NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT true,
    triggered_at    TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_price_alerts_user_id ON price_alerts(user_id);
CREATE INDEX idx_price_alerts_symbol ON price_alerts(stock_symbol);
CREATE INDEX idx_price_alerts_active ON price_alerts(stock_symbol, is_active);
