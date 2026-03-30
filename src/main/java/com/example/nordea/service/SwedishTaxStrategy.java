package com.example.nordea.service;

import com.example.nordea.model.TaxCountry;
import com.example.nordea.model.TaxInput;
import com.example.nordea.model.TaxResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class SwedishTaxStrategy implements TaxStrategy {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.01086");

    private static final int SCALE = 2;

    @Override
    public TaxResult calculate(TaxInput taxInput) {
        return new TaxResult(taxInput.accountID(),
                taxInput.totalInvestedAmount(),
                taxInput.totalInvestedAmount()
                        .multiply(TAX_RATE)
                        .setScale(SCALE, RoundingMode.HALF_UP),
                taxInput.currency(),
                "SE_ISK");
    }

    @Override
    public TaxCountry supportedCountry() {
        return TaxCountry.SE;
    }
}
