package co.com.bancolombia.integration;

import co.com.bancolombia.api.dto.ConfigurationRuleRequest;
import co.com.bancolombia.api.dto.ConfigurationRuleResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the complete CRUD lifecycle.
 * Requires DynamoDB Local running on localhost:8010 via Podman.
 *
 * Setup:
 *   ./scripts/start-dynamodb-local.sh
 *   ./scripts/create-dynamodb-table.sh
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class ConfigurationRuleCrudIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = webTestClient.mutate()
                .responseTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Test
    void testCompleteCrudLifecycle() {
        Map<String, Object> spec = Map.of(
                "matcher", Map.of("path", "/api/v1/orders", "method", "POST"),
                "target",  Map.of("url", "http://order-service:8080", "timeout", 5000)
        );

        ConfigurationRuleResponse created = webTestClient.post()
                .uri("/configuration-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ConfigurationRuleRequest("routing", "Order Routing", "order-domain", "active", spec))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ConfigurationRuleResponse.class)
                .returnResult().getResponseBody();

        assertNotNull(created);
        String id = created.id();
        assertEquals("routing", created.type());
        assertEquals(1, created.version());

        ConfigurationRuleResponse read = webTestClient.get()
                .uri("/configuration-rules/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ConfigurationRuleResponse.class)
                .returnResult().getResponseBody();

        assertNotNull(read);
        assertEquals(id, read.id());
        assertEquals(spec, read.spec());

        Map<String, Object> updatedSpec = Map.of(
                "matcher", Map.of("path", "/api/v2/orders", "method", "POST"),
                "target",  Map.of("url", "http://order-service-v2:8080", "timeout", 10000)
        );

        ConfigurationRuleResponse updated = webTestClient.put()
                .uri("/configuration-rules/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ConfigurationRuleRequest("routing", "Order Routing V2", "order-domain", "active", updatedSpec))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ConfigurationRuleResponse.class)
                .returnResult().getResponseBody();

        assertNotNull(updated);
        assertEquals(id, updated.id());
        assertEquals(2, updated.version());
        assertEquals("Order Routing V2", updated.name());
        assertEquals(updatedSpec, updated.spec());
        assertEquals(created.createdAt(), updated.createdAt());
        assertNotEquals(created.updatedAt(), updated.updatedAt());

        webTestClient.delete()
                .uri("/configuration-rules/{id}", id)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        webTestClient.get()
                .uri("/configuration-rules/{id}", id)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testUpdateNonExistentRule_shouldReturn404() {
        webTestClient.put()
                .uri("/configuration-rules/non-existent-id")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ConfigurationRuleRequest(
                        "routing", "Any", "any", "active", Map.of("path", "/test")))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testDeleteNonExistentRule_shouldReturn404() {
        webTestClient.delete()
                .uri("/configuration-rules/non-existent-id")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testChangeStatus_shouldIncrementVersion() {
        ConfigurationRuleResponse created = webTestClient.post()
                .uri("/configuration-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ConfigurationRuleRequest(
                        "routing", "Status Test", "test", "active",
                        Map.of("path", "/status-test")))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ConfigurationRuleResponse.class)
                .returnResult().getResponseBody();

        assertNotNull(created);
        assertEquals("active", created.status());

        ConfigurationRuleResponse updated = webTestClient.put()
                .uri("/configuration-rules/{id}", created.id())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ConfigurationRuleRequest(
                        "routing", "Status Test", "test", "inactive",
                        Map.of("path", "/status-test")))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ConfigurationRuleResponse.class)
                .returnResult().getResponseBody();

        assertNotNull(updated);
        assertEquals("inactive", updated.status());
        assertEquals(2, updated.version());
    }
}
