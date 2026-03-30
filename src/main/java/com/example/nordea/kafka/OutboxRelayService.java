package com.example.nordea.kafka;

import com.example.nordea.entity.OutboxEventEntity;
import com.example.nordea.repository.OutboxEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        value = "app.scheduling.enable", havingValue = "true", matchIfMissing = true
)
public class OutboxRelayService {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 500)
    @Transactional
    public void sendOutboxEvent() {
        List<OutboxEventEntity> events = outboxEventRepository.findByPublished(false);

        for (OutboxEventEntity event : events) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getPayload()).get();
                event.setPublished(true);
                event.setPublishedAt(Instant.now());
                outboxEventRepository.save(event);

            }  catch (Exception ex) {
                log.error("Failed to send event {}", event.getId(), ex);
                ex.printStackTrace();
            }
        }
    }
}
