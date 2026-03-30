package com.example.nordea;

import com.example.nordea.controller.AccountResponse;
import com.example.nordea.controller.AllocateInvestmentRequest;
import com.example.nordea.controller.CreateAccountRequest;
import com.example.nordea.entity.AccountEntity;
import com.example.nordea.entity.AuditLogEntity;
import com.example.nordea.entity.InvestmentEntity;
import com.example.nordea.entity.OutboxEventEntity;
import com.example.nordea.exception.AccountNotFoundException;
import com.example.nordea.exception.InsufficientFundsException;
import com.example.nordea.kafka.OutboxRelayService;
import com.example.nordea.model.AccountStatus;
import com.example.nordea.model.Currency;
import com.example.nordea.model.ProductType;
import com.example.nordea.model.TaxCountry;
import com.example.nordea.repository.AccountRepository;
import com.example.nordea.repository.AuditLogRepository;
import com.example.nordea.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class AccountFlowIntegrationTest extends AbstractIntegrationTest {

    public static final String FUND_CODE = "DKKAB";
    public static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private OutboxRelayService  outboxRelayService;

    @Test
    public void fullAccountFlow_createsAccount_allocatesInvestment_verifiesKafkaEvent() {
        // Step 1 — create account
        // POST /accounts with a valid CreateAccountRequest
        // assert 201, response body has a valid UUID id
        String holderName = "John Doe";
        TaxCountry taxCountry = TaxCountry.SE;
        ProductType productType = ProductType.SAVINGS;

        CreateAccountRequest createAccountRequest = new CreateAccountRequest(
                holderName,
                taxCountry,
                productType
        );

        ResponseEntity<AccountResponse> createResponse =
                restTemplate.postForEntity("/accounts", createAccountRequest, AccountResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID accountId = createResponse.getBody().getId();
        assertThat(accountId).isNotNull();

        // Step 2 — verify DB state
        // load the account from accountRepository
        // assert holderName, taxCountry, productType, status=ACTIVE

        AccountEntity account = accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
        assertThat(holderName).isEqualTo(account.getHolderName());
        assertThat(taxCountry).isEqualTo(account.getTaxCountry());
        assertThat(productType).isEqualTo(account.getProductType());
        assertThat(account.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);

        // Step 3 — allocate investment
        // POST /accounts/{id}/investments with a valid AllocateInvestmentRequest
        // assert 201

        AllocateInvestmentRequest allocateInvestmentRequest = new AllocateInvestmentRequest();
        allocateInvestmentRequest.setFundCode(FUND_CODE);
        allocateInvestmentRequest.setCurrency(Currency.GBP);
        allocateInvestmentRequest.setAmount(new BigDecimal("100"));

        String url = "/accounts/" + account.getId().toString() + "/investments";
        ResponseEntity<String> response = restTemplate.postForEntity(url, allocateInvestmentRequest, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("Investment allocated");


        // Step 4 — verify investment in DB
        // reload account, assert investments list has 1 entry
        // assert fundCode and amount match the request

        account = accountRepository.findByIdWithInvestments(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
        assertThat(account.getInvestmentEntities()).hasSize(1);
        InvestmentEntity investmentEntity = account.getInvestmentEntities().get(0);
        assertThat(investmentEntity.getFundCode()).isEqualTo(FUND_CODE);
        assertThat(investmentEntity.getAmount().getAmount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(investmentEntity.getAmount().getCurrency()).isEqualTo(Currency.GBP);

        // Step 5 — verify outbox
        // assert outboxEventRepository has 2 unpublished events
        // (one for ACCOUNT_CREATED, one for INVESTMENT_ALLOCATED)

        List<OutboxEventEntity> outboxEventEntities = outboxEventRepository.findByPublished(false);
        assertThat(outboxEventEntities).hasSize(2);
        assertThat(outboxEventEntities.stream().filter(e -> e.getEventType().equals("ACCOUNT_CREATED")).toList()).hasSize(1);
        assertThat(outboxEventEntities.stream().filter(e -> e.getEventType().equals("INVESTMENT_ALLOCATED")).toList()).hasSize(1);

        // Step 6 — wait for Kafka event
        // Use Awaitility to poll until auditLogRepository has 2 entries
        // assert eventType on each entry

        outboxRelayService.sendOutboxEvent();

        await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    List<AuditLogEntity> entities = auditLogRepository.findAll();
                    assertThat(entities).hasSize(2);
                    assertThat(entities.stream().filter(e -> e.getAction().equals("ACCOUNT_CREATED")).toList()).hasSize(1);
                    assertThat(entities.stream().filter(e -> e.getAction().equals("INVESTMENT_ALLOCATED")).toList()).hasSize(1);
                });

    }

    @Test
    void getAccount_withUnknownId_returns404WithProblemDetail() {
        // GET /accounts/{randomUUID}
        // assert status 404
        // assert response body: status=404, errorCode="ACCOUNT_NOT_FOUND"
        ResponseEntity<String> response = restTemplate.getForEntity("/accounts/" + UUID.randomUUID(), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        JsonNode node = MAPPER.readTree(response.getBody());
        String errorCode = node.get("errorCode").asString();
        assertThat(errorCode).isEqualTo("ACCOUNT_NOT_FOUND");

    }

    @Test
    void createAccount_withInvalidRequest_returns400WithViolations() {
        // POST /accounts with missing required fields
        // assert status 400
        // assert response body has "violations" property as a list
        // assert violations list contains an entry for the missing field
        CreateAccountRequest createAccountRequest = new CreateAccountRequest(null, TaxCountry.DE, ProductType.SAVINGS);
        ResponseEntity<String> response = restTemplate.postForEntity("/accounts", createAccountRequest, String.class);
        JsonNode root = MAPPER.readTree(response.getBody());
        JsonNode violations = root.get("violations");
        assertThat(violations).isNotNull();
        assertThat(violations.isArray()).isTrue();
        assertThat(violations).hasSize(1);

        JsonNode firstViolation = violations.get(0);

        String field = firstViolation.get("field").asString();
        String message = firstViolation.get("message").asString();

        assertThat(field).isEqualTo("holderName");
        assertThat(message).isEqualTo("must not be blank");
    }

    @Test
    void allocateInvestment_exceedingBalance_returns422() {
        // Create an account with a known balance
        // POST /accounts/{id}/investments with an amount exceeding the balance
        // assert status 422
        // assert errorCode="INSUFFICIENT_FUNDS"
        String holderName = "Jane Doe";
        TaxCountry taxCountry = TaxCountry.DE;
        ProductType productType = ProductType.PENSION;

        CreateAccountRequest createAccountRequest = new CreateAccountRequest(
                holderName,
                taxCountry,
                productType
        );

        ResponseEntity<AccountResponse> createResponse =
                restTemplate.postForEntity("/accounts", createAccountRequest, AccountResponse.class);

        AllocateInvestmentRequest allocateInvestmentRequest = new AllocateInvestmentRequest();
        allocateInvestmentRequest.setFundCode(FUND_CODE);
        allocateInvestmentRequest.setCurrency(Currency.GBP);
        allocateInvestmentRequest.setAmount(new BigDecimal("2000"));

        String url = "/accounts/" + createResponse.getBody().getId().toString() + "/investments";
        ResponseEntity<String> response = restTemplate.postForEntity(url, allocateInvestmentRequest, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);

        JsonNode root = MAPPER.readTree(response.getBody());
        assertThat(root.get("errorCode").asString()).isEqualTo(InsufficientFundsException.ERROR_CODE);

    }
}
