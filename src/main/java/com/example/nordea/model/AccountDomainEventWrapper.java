package com.example.nordea.model;

public record AccountDomainEventWrapper(
        AccountDomainEvent event,
        String topic
) {}