package com.example.nordea.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class PensionCoreException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    protected PensionCoreException(String message,
                                   String errorCode,
                                   HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    protected PensionCoreException(String message,
                                   String errorCode,
                                   HttpStatus httpStatus,
                                   Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

}
