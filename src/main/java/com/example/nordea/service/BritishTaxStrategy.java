package com.example.nordea.service;

import com.example.nordea.model.TaxCountry;
import com.example.nordea.model.TaxInput;
import com.example.nordea.model.TaxResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class BritishTaxStrategy implements TaxStrategy {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.2");
    private static final BigDecimal ALLOWANCE = new BigDecimal("20000");

    private static final int SCALE = 2;

    @Override
    public TaxResult calculate(TaxInput taxInput) {
        final BigDecimal totalInvestedAmount = taxInput.totalInvestedAmount();
        final BigDecimal tax = totalInvestedAmount.compareTo(ALLOWANCE) <= 0 ?
                BigDecimal.ZERO :
                totalInvestedAmount
                    .subtract(ALLOWANCE)
                    .multiply(TAX_RATE)
                    .setScale(SCALE, RoundingMode.HALF_UP);

        return new TaxResult(taxInput.accountID(),
                taxInput.totalInvestedAmount(),
                tax,
                taxInput.currency(),
                "UK_ISA");
    }

    @Override
    public TaxCountry supportedCountry() {
        return TaxCountry.UK;
    }
}
