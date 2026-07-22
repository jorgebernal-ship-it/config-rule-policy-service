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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Fidelity tests verifying that content survives the save→retrieve cycle intact.
 * Requires DynamoDB Local running on localhost:8010 via Podman.
 *
 * Setup:
 *   ./scripts/start-dynamodb-local.sh
 *   ./scripts/create-dynamodb-table.sh
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class ConfigurationRuleFidelityIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = webTestClient.mutate()
                .responseTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * Case 1: JSON structured fidelity (routing type).
     * Verifies nested Map equality — key order does not matter.
     */
    @Test
    void testJsonFidelity_RoutingType() {
        Map<String, Object> spec = Map.of(
                "matcher", Map.of(
                        "path",    "/api/v1/payments",
                        "method",  "POST",
                        "headers", Map.of("Content-Type", "application/json")
                ),
                "target", Map.of(
                        "url",     "http://payment-service:8080/process",
                        "timeout", 5000,
                        "retry",   Map.of("maxAttempts", 3, "backoff", "exponential")
                )
        );

        ConfigurationRuleResponseDTO created = webTestClient.post()
                .uri("/configuration-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ConfigurationRuleRequestDTO(
                        "routing", "Payment API Routing", "payment-domain", "active", spec))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ConfigurationRuleResponseDTO.class)
                .returnResult().getResponseBody();

        assertNotNull(created);

        ConfigurationRuleResponseDTO retrieved = webTestClient.get()
                .uri("/configuration-rules/{id}", created.id())
                .exchange()
                .expectStatus().isOk()
                .expectBody(ConfigurationRuleResponseDTO.class)
                .returnResult().getResponseBody();

        assertNotNull(retrieved);
        // Structural equality — key order is irrelevant
        assertEquals(spec, retrieved.spec(), "spec must be structurally equal after save→get cycle");

        @SuppressWarnings("unchecked")
        Map<String, Object> matcher = (Map<String, Object>) retrieved.spec().get("matcher");
        assertEquals("/api/v1/payments", matcher.get("path"));

        @SuppressWarnings("unchecked")
        Map<String, Object> target = (Map<String, Object>) retrieved.spec().get("target");
        assertEquals("http://payment-service:8080/process", target.get("url"));

        System.out.printf("✓ JSON Fidelity PASSED — spec keys preserved correctly%n");
    }

    /**
     * Case 2: Rego text-plain fidelity (policy-rego type).
     * Verifies byte-for-byte equality including newlines.
     */
    @Test
    void testRegoFidelity_PolicyRegoType() throws IOException, URISyntaxException {
        URL regoUrl = getClass().getClassLoader().getResource("fixtures/politica-ejemplo.rego");
        assertNotNull(regoUrl, "Test fixture 'politica-ejemplo.rego' must exist in test resources");
        Path regoPath      = Path.of(regoUrl.toURI());
        String original    = Files.readString(regoPath, StandardCharsets.UTF_8);
        long originalBytes = Files.size(regoPath);

        ConfigurationRuleResponseDTO created = webTestClient.post()
                .uri("/configuration-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ConfigurationRuleRequestDTO(
                        "policy-rego", "Resource Access Policy", "authz-domain", "active",
                        Map.of("content", original, "version", "1.0.0")))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ConfigurationRuleResponseDTO.class)
                .returnResult().getResponseBody();

        assertNotNull(created);

        // GET /raw — must return the exact string, no wrapping
        String raw = webTestClient.get()
                .uri("/configuration-rules/{id}/raw", created.id())
                .accept(MediaType.TEXT_PLAIN)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_PLAIN)
                .expectBody(String.class)
                .returnResult().getResponseBody();

        assertNotNull(raw);
        assertEquals(original, raw, "Retrieved raw content must be byte-for-byte identical to original");

        long retrievedBytes = raw.getBytes(StandardCharsets.UTF_8).length;
        assertEquals(originalBytes, retrievedBytes, "Byte size must match exactly");

        long originalNewlines  = original.chars().filter(c -> c == '\n').count();
        long retrievedNewlines = raw.chars().filter(c -> c == '\n').count();
        assertEquals(originalNewlines, retrievedNewlines, "Newline count must be preserved");

        // Also verify content in JSON response
        ConfigurationRuleResponseDTO json = webTestClient.get()
                .uri("/configuration-rules/{id}", created.id())
                .exchange()
                .expectStatus().isOk()
                .expectBody(ConfigurationRuleResponseDTO.class)
                .returnResult().getResponseBody();

        assertNotNull(json);
        assertEquals(original, json.spec().get("content"),
                "spec.content in JSON response must also be identical");

        System.out.printf("✓ Rego Fidelity PASSED — originalBytes=%d, retrievedBytes=%d, newlines=%d%n",
                originalBytes, retrievedBytes, retrievedNewlines);
    }

    @Test
    void testRawEndpoint_NonPolicyRegoType_shouldReturn404() {
        ConfigurationRuleResponseDTO created = webTestClient.post()
                .uri("/configuration-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ConfigurationRuleRequestDTO(
                        "routing", "Test Rule", "test", "active",
                        Map.of("path", "/test")))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ConfigurationRuleResponseDTO.class)
                .returnResult().getResponseBody();

        assertNotNull(created);

        webTestClient.get()
                .uri("/configuration-rules/{id}/raw", created.id())
                .exchange()
                .expectStatus().isNotFound();

        System.out.println("✓ /raw on non-policy-rego correctly returns 404");
    }

    @Test
    void testCreate_PolicyRegoWithoutContent_shouldReturn400() {
        webTestClient.post()
                .uri("/configuration-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ConfigurationRuleRequestDTO(
                        "policy-rego", "Invalid", "authz", "active",
                        Map.of("version", "1.0.0")))
                .exchange()
                .expectStatus().isBadRequest();

        System.out.println("✓ policy-rego without content correctly returns 400");
    }
}
