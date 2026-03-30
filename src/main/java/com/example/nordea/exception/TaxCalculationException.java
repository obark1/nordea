package com.example.nordea.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class TaxCalculationException extends PensionCoreException {
    public static final String ERROR_CODE = "TAX_CALCULATION_FAILED";
    public static final HttpStatus HTTP_STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

    public TaxCalculationException(UUID accountId, String reason) {
        super(String.format("Tax calculation failed for account %s: %s", accountId, reason),
                ERROR_CODE,
                HTTP_STATUS);
    }
}
