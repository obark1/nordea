package com.example.nordea.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String BASE_TYPE_URI = "https://pensioncore.io/errors/";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @ExceptionHandler(PensionCoreException.class)
    public ResponseEntity<ProblemDetail> handleDomainException(PensionCoreException ex, HttpServletRequest request) {

        ProblemDetail problem = baseProblemDetail(ex.getHttpStatus().value(),
                ex.getErrorCode(),
                toTitle(ex.getErrorCode()),
                ex.getMessage(),
                request);

        problem.setType(URI.create(BASE_TYPE_URI + toHyphen(ex.getErrorCode())));

        return ResponseEntity.status(ex.getHttpStatus()).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        List<Map<String, String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", error.getDefaultMessage()
                ))
                .toList();

        ProblemDetail problem = baseProblemDetail(ex.getStatusCode().value(),
                "VALIDATION_ERROR",
                "Validation Failed",
                "Request validation failed",
                request);

        problem.setProperty("violations", errors);
        problem.setType(URI.create(BASE_TYPE_URI + "validation-error"));

        return ResponseEntity.status(ex.getStatusCode()).body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        List<Map<String, String>> errors = ex.getConstraintViolations()
                .stream()
                .map(violation -> Map.of(
                        "field", extractFieldName(violation.getPropertyPath().toString()),
                        "message", violation.getMessage()
                ))
                .toList();

        ProblemDetail problem = baseProblemDetail(HttpStatus.BAD_REQUEST.value(),
                "CONSTRAINT_VIOLATION",
                "Constraint Validation",
                "Constraint validation failed",
                request);

        problem.setProperty("violations", errors);
        problem.setType(URI.create(BASE_TYPE_URI + "constraint-violation"));

        return ResponseEntity.status(problem.getStatus()).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at [{}]", request.getRequestURI(), ex);
        ProblemDetail problem = baseProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "Internal Server Error",
                "An unexpected error occurred",
                request);

        problem.setType(URI.create(BASE_TYPE_URI + "internal-server-error"));
        return ResponseEntity.status(problem.getStatus()).body(problem);
    }

    private ProblemDetail baseProblemDetail(int status, String errorCode,
                                            String title, String detail,
                                            HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setTitle(title);
        problem.setDetail(detail);
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("errorCode", errorCode);
        problem.setProperty("timestamp", ZonedDateTime.now(ZoneId.of("UTC")).format(FORMATTER));

        return problem;
    }

    private String extractFieldName(String path) {
        if (path == null) return "";

        int lastDot = path.lastIndexOf('.');
        return (lastDot != -1) ? path.substring(lastDot + 1) : path;
    }

    private String toTitle(String errorCode) {
        return Arrays.stream(errorCode.toLowerCase().split("_"))
                .map(s -> s.substring(0,1).toUpperCase() + s.substring(1))
                .collect(Collectors.joining(" "));
    }

    private String toHyphen(String errorCode) {
        return errorCode.toLowerCase().replace('_', '-');
    }

}
