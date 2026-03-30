package com.example.nordea.model;

import java.math.BigDecimal;
import java.util.UUID;

public record TaxInput(UUID accountID,
                       TaxCountry taxCountry,
                       ProductType productType,
                       BigDecimal totalInvestedAmount,
                       Currency currency,
                       int taxYear
) {
}
