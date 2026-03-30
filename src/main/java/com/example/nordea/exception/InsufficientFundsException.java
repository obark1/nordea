package com.example.nordea.exception;

import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientFundsException extends PensionCoreException {

    public static final String ERROR_CODE = "INSUFFICIENT_FUNDS";
    public static final HttpStatus HTTP_STATUS = HttpStatus.UNPROCESSABLE_CONTENT;

    public InsufficientFundsException(UUID accountId, BigDecimal requiredAmount, BigDecimal availableAmount) {
        super(String.format("Insufficient funds on account %s: required %s, available %s", accountId, requiredAmount, availableAmount),
                ERROR_CODE,
                HTTP_STATUS);
    }
}
