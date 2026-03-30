package com.example.nordea.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "audit_log")
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID eventId;

    private UUID entityId; // accountId

    private String action; // eventType

    private String status; // SUCCESS / FAILED

    private String topic;

    private Integer partitionId;

    private Long recordOffset;

    private Instant eventTime;     // occurredAt from event
    private Instant processedAt;    // when YOU handled it

    private String consumerService;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private String errorMessage; // if failed

    @CreationTimestamp
    private Instant createdAt;
}

