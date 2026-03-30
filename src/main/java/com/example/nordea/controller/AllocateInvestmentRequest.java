package com.example.nordea.controller;

import com.example.nordea.model.Currency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AllocateInvestmentRequest {

    @NotNull
    @Pattern(regexp =  "^[A-Z]{2,6}$")
    private String fundCode;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    private Currency currency;

}
