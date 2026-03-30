package com.example.nordea.service;

import com.example.nordea.model.TaxCountry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TaxStrategyRegistry {
    private final Map<TaxCountry, TaxStrategy> strategies;

    public TaxStrategyRegistry(final List<TaxStrategy> strategies) {
        this.strategies = strategies.stream()
                .collect(Collectors.toMap(
                        TaxStrategy::supportedCountry,
                        Function.identity()
                ));
        System.out.println("XXX:" + strategies);
    }

    public TaxStrategy resolve(final TaxCountry country) {
        TaxStrategy strategy = strategies.get(country);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy for " + country);
        }
        return strategy;
    }
}
