package com.example.nordea.service;

import com.example.nordea.entity.OutboxEventEntity;
import com.example.nordea.exception.TaxCalculationException;
import com.example.nordea.model.AccountDomainEvent;
import com.example.nordea.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public void publish(AccountDomainEvent accountDomainEvent, String topic) {
        String json;
        try {
            json = objectMapper.writeValueAsString(accountDomainEvent);
        } catch (JacksonException e) {
            throw new TaxCalculationException(accountDomainEvent.eventId(), "Failed to serialize domain event: " + e.getMessage());
        }
        OutboxEventEntity outboxEvent = createOutboxEventEntity(accountDomainEvent, topic, json);

        outboxEventRepository.save(outboxEvent);
    }

    private static @NonNull OutboxEventEntity createOutboxEventEntity(AccountDomainEvent accountDomainEvent,
                                                                      String topic, String json) {
        OutboxEventEntity outboxEvent = new OutboxEventEntity();
        outboxEvent.setPayload(json);
        outboxEvent.setTopic(topic);
        outboxEvent.setEventType(accountDomainEvent.eventType());
        outboxEvent.setPublished(false);
        return outboxEvent;
    }
}
