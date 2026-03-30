package com.example.nordea.entity;

import com.example.nordea.model.Currency;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class Money {
    private final BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private final Currency currency;

    public Money(final BigDecimal amount, final Currency currency) {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        this.amount = amount;
        this.currency = currency;
    }

    protected Money() {
        // JPA only — do not use
        this.amount = null;
        this.currency = null;
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public boolean isZeroOrNegative() {
        return amount == null || amount.compareTo(BigDecimal.ZERO) <= 0;
    }

    public Money add(Money other) {
        Objects.requireNonNull(other, "other must not be null");
        if (this.currency != other.currency) {
            throw new IllegalArgumentException(
                    "Currency mismatch: cannot add %s to %s".formatted(other.currency, this.currency)
            );
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
