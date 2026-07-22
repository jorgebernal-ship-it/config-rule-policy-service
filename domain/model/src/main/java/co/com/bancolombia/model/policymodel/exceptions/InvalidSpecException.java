package co.com.bancolombia.model.policymodel.exceptions;

public class InvalidSpecException extends RuntimeException {
    
    public InvalidSpecException(String message) {
        super(message);
    }
    
    public InvalidSpecException(String message, Throwable cause) {
        super(message, cause);
    }
}
