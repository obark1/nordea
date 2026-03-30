package com.example.nordea.service;

import com.example.nordea.entity.AccountEntity;
import com.example.nordea.exception.AccountNotFoundException;
import com.example.nordea.model.TaxInput;
import com.example.nordea.model.TaxResult;
import com.example.nordea.repository.AccountRepository;
import com.example.nordea.repository.TaxRuleRepository;
import com.example.nordea.util.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
public class TaxCalculationService {
    private final TaxStrategyRegistry registry;
    private final AccountRepository accountRepository;
    private final TaxRuleRepository taxRuleRepository;
    private final Executor executor;

    public TaxResult calculateTax(UUID accountId, int taxYear) {
        // 1. Load account or throw AccountNotFoundException
        CompletableFuture<AccountEntity> accountFuture = CompletableFuture
                .supplyAsync(() -> accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId)),
                        executor);
        AccountEntity account;
        try {
            account = accountFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 2. Build TaxInput from account state
        BigDecimal totalInvested = account.getInvestmentEntities()
                .stream()
                .map(i -> Utils.convertToBaseCurrency(i.getAmount(), account.getBalance().getCurrency()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        TaxInput taxInput = new TaxInput(accountId, account.getTaxCountry(), account.getProductType(), totalInvested, account.getBalance().getCurrency(), taxYear);

        // 3. Resolve strategy from registry
        TaxStrategy taxStrategy = registry.resolve(account.getTaxCountry());

        // 4. Delegate to strategy.calculate(input)

        // 5. Return TaxResult
        return taxStrategy.calculate(taxInput);
    }


}


