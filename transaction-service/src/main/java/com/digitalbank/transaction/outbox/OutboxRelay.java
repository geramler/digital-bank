package com.digitalbank.transaction.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxRelay(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, Object> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void relay() {
        List<OutboxEvent> pending = outboxEventRepository.findByPublishedAtIsNullOrderByCreatedAtAsc();
        for (OutboxEvent event : pending) {
            try {
                Object payload = objectMapper.readValue(event.getPayload(), Class.forName(event.getPayloadType()));
                kafkaTemplate.send(event.getTopic(), event.getMessageKey(), payload).get(10, TimeUnit.SECONDS);
                event.setPublishedAt(Instant.now());
                outboxEventRepository.save(event);
                log.info("Relayed outbox event id={} topic={} key={}", event.getId(), event.getTopic(), event.getMessageKey());
            } catch (Exception ex) {
                log.error("Failed to relay outbox event id={}: {}", event.getId(), ex.getMessage(), ex);
            }
        }
    }
}
