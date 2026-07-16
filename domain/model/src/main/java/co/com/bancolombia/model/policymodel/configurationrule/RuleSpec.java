package co.com.bancolombia.model.policymodel.configurationrule;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder(toBuilder = true)
public class RuleSpec {

    // --- ESPECÍFICO PARA REGO_POLICY ---
    /**
     * El contenido en texto plano del archivo .rego
     */
    private final String regoContent;

    /**
     * El checksum o SHA del archivo (vital para la PoC de GitHub)
     */
    private final String sha;

    // --- ESPECÍFICO PARA ENRUTAMIENTO O PIP ---
    /**
     * La ruta del archivo o el path destino (Ej: "/api/v1/payments")
     */
    private final String targetPath;

    // --- GENÉRICO PARA CUALQUIER CONFIGURACIÓN (Key-Value) ---
    /**
     * Parámetros dinámicos en formato Clave-Valor.
     * Si es un PIP, aquí pueden ir credenciales codificadas o URLs.
     * Si son Acciones Post, aquí pueden ir los templates de SMS/Email.
     */
    private final Map<String, Object> parameters;
}