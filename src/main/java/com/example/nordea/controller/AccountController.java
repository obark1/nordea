package com.example.nordea.controller;

import com.example.nordea.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.util.UUID;

@RestController
@Validated
@RequiredArgsConstructor
public class AccountController {

    private static final String BAD_REQUEST = "Validation error (RFC 7807)";
    private static final String NOT_FOUND = "Resource not found";
    private static final String CONFLICT = "Duplicate Resource";
    private static final String UNPROCESSABLE_ENTITY = "Business rule violation (e.g. insufficient funds)";

    private final AccountService accountService;

    @PostMapping("/accounts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful account creation"),
            @ApiResponse(responseCode = "400", description = BAD_REQUEST),
            @ApiResponse(responseCode = "409", description = CONFLICT)
    }
    )
    @Operation(summary = "Open a new pension account")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest account) {
        return new ResponseEntity<>(accountService.createAccount(account), HttpStatus.CREATED);
    }

    @GetMapping("/accounts/{accountId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful account fetch"),
            @ApiResponse(responseCode = "404", description = NOT_FOUND)
    }
    )
    @Operation(summary = "Get pension account by id")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable UUID accountId) {
        return new ResponseEntity<>(accountService.getAccount(accountId), HttpStatus.OK);
    }

    @PostMapping("/accounts/{accountId}/investments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Investment allocated"),
            @ApiResponse(responseCode = "400", description = BAD_REQUEST),
            @ApiResponse(responseCode = "404", description = NOT_FOUND),
            @ApiResponse(responseCode = "422", description = UNPROCESSABLE_ENTITY)
    }
    )
    @Operation(summary = "Allocate an investment to an account")
    public ResponseEntity<String> allocateInvestments(@PathVariable UUID accountId, @Valid @RequestBody AllocateInvestmentRequest investmentRequest) {
        accountService.allocateInvestments(accountId, investmentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body("Investment allocated");
    }

    @GetMapping("/accounts/{accountId}/tax-summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tax summary"),
            @ApiResponse(responseCode = "404", description = NOT_FOUND),
    }
    )
    @Operation(summary = "Get annual tax summary for an account")
    public ResponseEntity<TaxSummaryResponse> getTaxSummary(@PathVariable UUID accountId, @Min(2000) @Max(2100) @RequestParam Integer taxYear) throws AccountNotFoundException {
        return new ResponseEntity<>(accountService.getTaxSummary(accountId, taxYear), HttpStatus.OK);
    }

}
