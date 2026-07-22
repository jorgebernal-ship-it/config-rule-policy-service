package co.com.bancolombia.model.policystoragesthreemodel;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum ContentFormat {
    BASE64,
    TEXT;

    public static ContentFormat fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BASE64;
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            String validValues = Arrays.stream(values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException(
                    "Formato de contenido no valido: '" + value + "'. Valores aceptados: " + validValues);
        }
    }
}

