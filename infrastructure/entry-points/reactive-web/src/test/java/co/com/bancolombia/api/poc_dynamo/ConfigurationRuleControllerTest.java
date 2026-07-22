package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.ConfigurationRuleRequest;
import co.com.bancolombia.api.dto.ConfigurationRuleResponse;
import co.com.bancolombia.model.policymodel.ConfigurationRule;
import co.com.bancolombia.model.policymodel.configurationrule.ConfigurationType;
import co.com.bancolombia.model.policymodel.configurationrule.Status;
import co.com.bancolombia.model.policymodel.exceptions.ConfigurationRuleNotFoundException;
import co.com.bancolombia.model.policymodel.exceptions.InvalidSpecException;
import co.com.bancolombia.model.policymodel.exceptions.RawContentNotAvailableException;
import co.com.bancolombia.usecase.configurationrule.ConfigurationRuleUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {RouterRest.class, ConfigurationRuleHandler.class})
class ConfigurationRuleControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ConfigurationRuleUseCase useCase;

    private ConfigurationRule sampleRoutingRule;
    private ConfigurationRule samplePolicyRule;
    private static final String RULE_ID = "test-uuid-1234";

    @BeforeEach
    void setUp() {
        sampleRoutingRule = ConfigurationRule.builder()
                .id(RULE_ID)
                .type(ConfigurationType.ROUTING)
                .name("Payment Routing")
                .scope("payment-domain")
                .status(Status.ACTIVE)
                .spec(Map.of("matcher", Map.of("path", "/api/v1/payments"),
                             "target",  Map.of("url", "http://payment-svc:8080")))
                .createdAt(Instant.parse("2026-07-17T10:00:00Z"))
                .updatedAt(Instant.parse("2026-07-17T10:00:00Z"))
                .version(1)
                .build();

        samplePolicyRule = ConfigurationRule.builder()
                .id(RULE_ID)
                .type(ConfigurationType.POLICY_REGO)
                .name("Auth Policy")
                .scope("authz")
                .status(Status.ACTIVE)
                .spec(Map.of("content", "package authz\n\ndefault allow := false"))
                .createdAt(Instant.parse("2026-07-17T10:00:00Z"))
                .updatedAt(Instant.parse("2026-07-17T10:00:00Z"))
                .version(1)
                .build();
    }

    // ─── CREATE ─────────────────────────────────────────────────────────────

    @Test
    void createRule_shouldReturn201WithBody() {
        when(useCase.create(any())).thenReturn(Mono.just(sampleRoutingRule));

        ConfigurationRuleRequest request = new ConfigurationRuleRequest(
                "routing", "Payment Routing", "payment-domain", "active",
                Map.of("matcher", Map.of("path", "/api/v1/payments")));

        webTestClient.post()
                .uri("/configuration-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ConfigurationRuleResponse.class)
                .value(response -> {
                    assert response.id().equals(RULE_ID);
                    assert response.type().equals("routing");
                    assert response.version() == 1;
                });
    }

    @Test
    void createRule_whenInvalidSpec_shouldReturn400() {
        when(useCase.create(any()))
                .thenReturn(Mono.error(new InvalidSpecException(
                        "For type 'policy-rego', spec.content must exist and cannot be blank")));

        ConfigurationRuleRequest request = new ConfigurationRuleRequest(
                "policy-rego", "Bad Policy", "authz", "active",
                Map.of("version", "1.0.0"));

        webTestClient.post()
                .uri("/configuration-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    // ─── GET ────────────────────────────────────────────────────────────────

    @Test
    void getRule_shouldReturn200WithBody() {
        when(useCase.getById(RULE_ID)).thenReturn(Mono.just(sampleRoutingRule));

        webTestClient.get()
                .uri("/configuration-rules/{id}", RULE_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ConfigurationRuleResponse.class)
                .value(response -> {
                    assert response.id().equals(RULE_ID);
                    assert response.name().equals("Payment Routing");
                });
    }

    @Test
    void getRule_whenNotFound_shouldReturn404() {
        when(useCase.getById(anyString()))
                .thenReturn(Mono.error(new ConfigurationRuleNotFoundException("unknown-id")));

        webTestClient.get()
                .uri("/configuration-rules/{id}", "unknown-id")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    // ─── GET RAW ────────────────────────────────────────────────────────────

    @Test
    void getRaw_shouldReturnPlainText() {
        String regoContent = "package authz\n\ndefault allow := false";
        when(useCase.getRawContent(RULE_ID)).thenReturn(Mono.just(regoContent));

        webTestClient.get()
                .uri("/configuration-rules/{id}/raw", RULE_ID)
                .accept(MediaType.TEXT_PLAIN)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_PLAIN)
                .expectBody(String.class)
                .isEqualTo(regoContent);
    }

    @Test
    void getRaw_whenNotPolicyRego_shouldReturn404() {
        when(useCase.getRawContent(anyString()))
                .thenReturn(Mono.error(new RawContentNotAvailableException(
                        "Raw content is only available for type 'policy-rego'")));

        // No Accept header — the error handler responds JSON regardless
        webTestClient.get()
                .uri("/configuration-rules/{id}/raw", RULE_ID)
                .exchange()
                .expectStatus().isNotFound();
    }

    // ─── UPDATE ─────────────────────────────────────────────────────────────

    @Test
    void updateRule_shouldReturn200WithIncrementedVersion() {
        ConfigurationRule updatedRule = sampleRoutingRule.toBuilder()
                .name("Payment Routing V2")
                .updatedAt(Instant.parse("2026-07-17T11:00:00Z"))
                .version(2)
                .build();

        when(useCase.update(eq(RULE_ID), any())).thenReturn(Mono.just(updatedRule));

        ConfigurationRuleRequest request = new ConfigurationRuleRequest(
                "routing", "Payment Routing V2", "payment-domain", "active",
                Map.of("matcher", Map.of("path", "/api/v2/payments")));

        webTestClient.put()
                .uri("/configuration-rules/{id}", RULE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ConfigurationRuleResponse.class)
                .value(response -> {
                    assert response.id().equals(RULE_ID);
                    assert response.name().equals("Payment Routing V2");
                    assert response.version() == 2;
                });
    }

    @Test
    void updateRule_whenNotFound_shouldReturn404() {
        when(useCase.update(anyString(), any()))
                .thenReturn(Mono.error(new ConfigurationRuleNotFoundException("unknown-id")));

        ConfigurationRuleRequest request = new ConfigurationRuleRequest(
                "routing", "Any Name", "any-scope", "active",
                Map.of("path", "/test"));

        webTestClient.put()
                .uri("/configuration-rules/{id}", "unknown-id")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateRule_whenInvalidSpec_shouldReturn400() {
        when(useCase.update(anyString(), any()))
                .thenReturn(Mono.error(new InvalidSpecException(
                        "For type 'policy-rego', spec.content must exist and cannot be blank")));

        ConfigurationRuleRequest request = new ConfigurationRuleRequest(
                "policy-rego", "Bad Policy", "authz", "active",
                Map.of("version", "2.0.0"));

        webTestClient.put()
                .uri("/configuration-rules/{id}", RULE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    // ─── DELETE ─────────────────────────────────────────────────────────────

    @Test
    void deleteRule_shouldReturn204NoContent() {
        when(useCase.delete(RULE_ID)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/configuration-rules/{id}", RULE_ID)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }

    @Test
    void deleteRule_whenNotFound_shouldReturn404() {
        when(useCase.delete(anyString()))
                .thenReturn(Mono.error(new ConfigurationRuleNotFoundException("unknown-id")));

        webTestClient.delete()
                .uri("/configuration-rules/{id}", "unknown-id")
                .exchange()
                .expectStatus().isNotFound();
    }
}
