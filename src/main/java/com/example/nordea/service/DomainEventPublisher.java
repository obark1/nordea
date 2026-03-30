package com.example.nordea.service;

import com.example.nordea.entity.OutboxEventEntity;
import com.example.nordea.exception.TaxCalculationException;
import com.example.nordea.model.AccountDomainEventWrapper;
import com.example.nordea.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publish(AccountDomainEventWrapper accountDomainEventWrapper) {
        String json;
        try {
            json = objectMapper.writeValueAsString(accountDomainEventWrapper.event());
        } catch (JacksonException e) {
            throw new TaxCalculationException(accountDomainEventWrapper.event().eventId(), "Failed to serialize domain event: " + e.getMessage());
        }
        OutboxEventEntity outboxEvent = createOutboxEventEntity(accountDomainEventWrapper, json);

        outboxEventRepository.save(outboxEvent);
    }

    private static @NonNull OutboxEventEntity createOutboxEventEntity(AccountDomainEventWrapper wrapper, String json) {
        OutboxEventEntity outboxEvent = new OutboxEventEntity();
        outboxEvent.setPayload(json);
        outboxEvent.setTopic(wrapper.topic());;
        outboxEvent.setEventType(wrapper.event().eventType());
        outboxEvent.setPublished(false);
        return outboxEvent;
    }
}
