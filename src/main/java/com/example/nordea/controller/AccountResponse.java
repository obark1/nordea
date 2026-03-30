package com.example.nordea.controller;

import com.example.nordea.model.AccountStatus;
import com.example.nordea.model.Currency;
import com.example.nordea.model.ProductType;
import com.example.nordea.model.TaxCountry;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class AccountResponse {

    private UUID id;
    private String holderName;
    private TaxCountry taxCountry;
    private ProductType productType;
    private AccountStatus accountStatus;
    private BigDecimal balance;
    private Currency currency;
    private Instant createdAt;
    private Long version;
}
