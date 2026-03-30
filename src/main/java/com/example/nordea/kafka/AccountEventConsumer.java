package com.example.nordea.kafka;

import com.example.nordea.entity.AuditLogEntity;
import com.example.nordea.model.AccountDomainEvent;
import com.example.nordea.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEventConsumer {
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "account.events", groupId = "pension-core")
    public void consume(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic, @Header(KafkaHeaders.OFFSET) long offset, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        try {
            AccountDomainEvent accountDomainEvent = objectMapper.readValue(message, AccountDomainEvent.class);
            auditLogRepository.save(AuditLogEntity.builder()
                    .eventId(accountDomainEvent.eventId())
                    .entityId(accountDomainEvent.accountId())
                    .action(accountDomainEvent.eventType())
                    .topic(topic)
                    .partitionId(partition)
                    .recordOffset(offset)
                    .eventTime(accountDomainEvent.occurredAt())
                    .status("SUCCESS")
                    .processedAt(Instant.now())
                    .consumerService("AccountEventConsumer")
                    .payload(message)
                    .build()
            );

        } catch (JacksonException e) {
            log.error("Error deserializing kafka message on topic [{}]: {}", topic, e.getMessage());
            throw new RuntimeException("Deserialization failed", e);
        } catch (RuntimeException e) {
            log.error("Failed to process kafka message on topic [{}]", topic, e);
            throw e;
        }

    }

    @DltHandler
    public void handleDlt(String message,
                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                          @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage) {
        log.error("DLT messaged received on topic [{}] - exception: {} - payload: {}", topic, message, exceptionMessage);
    }

}
