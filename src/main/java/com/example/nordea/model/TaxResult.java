package com.example.nordea.model;

import java.math.BigDecimal;
import java.util.UUID;

public record TaxResult(
        UUID accountId,
        BigDecimal taxableIncome,
        BigDecimal taxDue,
        Currency currency,
        String appliedStrategy  // e.g. "SE_ISK", "UK_ISA", "DE_PENSION"
) {
}
