package com.example.nordea.model;

import java.time.Instant;
import java.util.UUID;

public record AccountDomainEvent(
        UUID eventId,
        UUID accountId,
        String eventType,    // "ACCOUNT_CREATED", "INVESTMENT_ALLOCATED"
        String payload,      // JSON snapshot of relevant state
        Instant occurredAt
) {}