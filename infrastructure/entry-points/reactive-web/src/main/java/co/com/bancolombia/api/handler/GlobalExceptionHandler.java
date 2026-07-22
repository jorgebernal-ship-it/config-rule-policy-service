package co.com.bancolombia.api.handler;

import co.com.bancolombia.model.policymodel.exceptions.ConfigurationRuleNotFoundException;
import co.com.bancolombia.model.policymodel.exceptions.InvalidSpecException;
import co.com.bancolombia.model.policymodel.exceptions.RawContentNotAvailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConfigurationRuleNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleConfigurationRuleNotFound(ConfigurationRuleNotFoundException ex) {
        log.error("Configuration rule not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(RawContentNotAvailableException.class)
    public ResponseEntity<Map<String, Object>> handleRawContentNotAvailable(RawContentNotAvailableException ex) {
        log.error("Raw content not available: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(InvalidSpecException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidSpec(InvalidSpecException ex) {
        log.error("Invalid spec: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
    }

    private Map<String, Object> buildErrorResponse(HttpStatus status, String message) {
        return Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message
        );
    }
}
