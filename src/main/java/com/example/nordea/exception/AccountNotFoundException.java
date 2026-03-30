package com.example.nordea.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class AccountNotFoundException extends PensionCoreException {

    public static final String ERROR_CODE = "ACCOUNT_NOT_FOUND";
    public static final HttpStatus HTTP_STATUS = HttpStatus.NOT_FOUND;

    public AccountNotFoundException(UUID accountId) {
        super(String.format("Account %s was not found", accountId),
                ERROR_CODE,
                HTTP_STATUS);
    }
}
