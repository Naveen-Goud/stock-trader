package com.trading.trading.metrics;

import com.trading.trading.repository.OutboxEventRepository;
import com.trading.trading.entity.OutboxEvent;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxMetrics {

    private final OutboxEventRepository outboxEventRepository;
    private final MeterRegistry meterRegistry;

    @PostConstruct
    public void registerGauges() {
        Gauge.builder("outbox_pending_events_total", outboxEventRepository,
                repo -> repo.findTop100ByStatusOrderByCreatedAtAsc(OutboxEvent.OutboxStatus.PENDING).size())
            .description("Number of outbox events awaiting publish to Kafka")
            .register(meterRegistry);
    }
}
