package com.example.nordea.service;

import com.example.nordea.controller.AccountResponse;
import com.example.nordea.controller.AllocateInvestmentRequest;
import com.example.nordea.controller.CreateAccountRequest;
import com.example.nordea.controller.TaxSummaryResponse;
import com.example.nordea.entity.AccountEntity;
import com.example.nordea.entity.InvestmentEntity;
import com.example.nordea.entity.Money;
import com.example.nordea.exception.AccountNotFoundException;
import com.example.nordea.exception.InsufficientFundsException;
import com.example.nordea.model.*;
import com.example.nordea.repository.AccountRepository;
import com.example.nordea.util.Utils;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    public static final String TOPIC_ACCOUNT_EVENTS = "account.events";
    private final TaxCalculationService taxCalculationService;
    private final AccountRepository accountRepository;
    private final DomainEventPublisher domainEventPublisher;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public AccountResponse createAccount(final CreateAccountRequest account) {
        AccountEntity newAccount = new AccountEntity();
        newAccount.setHolderName(account.getHolderName());
        newAccount.setTaxCountry(account.getTaxCountry());
        newAccount.setProductType(account.getProductType());
        newAccount.setBalance(new Money(new BigDecimal("10000"), Currency.SEK));
        newAccount.setVersion(1L);
        newAccount.setAccountStatus(AccountStatus.ACTIVE);
        newAccount = accountRepository.saveAndFlush(newAccount);

        applicationEventPublisher.publishEvent(new AccountDomainEventWrapper(
                new AccountDomainEvent(
                        UUID.randomUUID(),
                        newAccount.getId(),
                        "ACCOUNT_CREATED",
                        objectMapper.writeValueAsString(newAccount),
                        Instant.now()
                ),
                TOPIC_ACCOUNT_EVENTS));

        return toResponse(newAccount);
    }

    public AccountResponse getAccount(final UUID accountId) {
        AccountEntity account = accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));

        return new AccountResponse(accountId, account.getHolderName(), account.getTaxCountry(),
                account.getProductType(), account.getAccountStatus(), account.getBalance().getAmount(),
                account.getBalance().getCurrency(), account.getCreatedAt(), account.getVersion());
    }

    @Transactional
    public void allocateInvestments(final UUID accountId, AllocateInvestmentRequest investmentRequest) {
        AccountEntity account = accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
        Currency currency = investmentRequest.getCurrency();
        BigDecimal baseAmount;
        baseAmount = Utils.convertToBaseCurrency(new Money(investmentRequest.getAmount(), currency), Currency.SEK);

        if (baseAmount.compareTo(account.getBalance().getAmount()) > 0) {
            throw new InsufficientFundsException(accountId, baseAmount, account.getBalance().getAmount());
        }

        InvestmentEntity investmentEntity = new InvestmentEntity();
        investmentEntity.setAccount(account);
        investmentEntity.setFundCode(investmentRequest.getFundCode());
        investmentEntity.setAmount(new Money(investmentRequest.getAmount(), currency));
        investmentEntity.setAllocatedAt(Instant.now());
        account.getInvestmentEntities().add(investmentEntity);

        account = accountRepository.saveAndFlush(account);

        applicationEventPublisher.publishEvent(new AccountDomainEventWrapper(
                new AccountDomainEvent(
                        UUID.randomUUID(),
                        account.getId(),
                        "INVESTMENT_ALLOCATED",
                        objectMapper.writeValueAsString(account),
                        Instant.now()
                ),
                TOPIC_ACCOUNT_EVENTS));
    }

    public TaxSummaryResponse getTaxSummary(final UUID accountId, final Integer taxYear) {
        TaxResult taxResult = taxCalculationService.calculateTax(accountId, taxYear);
        return new TaxSummaryResponse(accountId, taxYear, taxResult.taxableIncome(), taxResult.taxDue(), taxResult.currency(), taxResult.appliedStrategy());
    }

    private static @NonNull AccountResponse toResponse(AccountEntity account) {
        return new AccountResponse(account.getId(),
                account.getHolderName(),
                account.getTaxCountry(),
                account.getProductType(),
                account.getAccountStatus(),
                account.getBalance().getAmount(),
                account.getBalance().getCurrency(),
                account.getCreatedAt(),
                account.getVersion());
    }
}
