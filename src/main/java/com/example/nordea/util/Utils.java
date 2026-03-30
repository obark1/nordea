package com.example.nordea.util;

import com.example.nordea.entity.Money;
import com.example.nordea.model.Currency;

import java.math.BigDecimal;
import java.util.Map;

public class Utils {
    public static String initCap(String input) {
        String[] words = input.toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return result.toString().trim();
    }

    public static BigDecimal convertToBaseCurrency(Money amount, Currency baseCurrency) {
        if (amount.getCurrency().equals(baseCurrency)) {
            return amount.getAmount();
        }

        Map<Currency, BigDecimal> fxRates = Map.of(
                Currency.GBP, new BigDecimal("13.20"),
                Currency.EUR, new BigDecimal("11.40"),
                Currency.SEK, BigDecimal.ONE
        );

        BigDecimal baseCurrencyRate = fxRates.getOrDefault(amount.getCurrency(), BigDecimal.ONE);
        return amount.getAmount().multiply(baseCurrencyRate);
    }
}
