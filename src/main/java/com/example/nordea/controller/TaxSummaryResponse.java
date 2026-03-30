package com.example.nordea.controller;

import com.example.nordea.model.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
public class TaxSummaryResponse {

    private UUID accountId;
    private Integer taxYear;
    private BigDecimal taxableIncome;
    private BigDecimal taxDue;
    private Currency currency;
    private String appliedStrategy;
}
