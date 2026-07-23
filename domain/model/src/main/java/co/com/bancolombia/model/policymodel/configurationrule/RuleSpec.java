package co.com.bancolombia.model.policymodel.configurationrule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class RuleSpec {

    // --- ESPECÍFICO PARA REGO_POLICY ---
    /**
     * El contenido en texto plano del archivo .rego
     */
    private String regoContent; // ◄--- Quitamos el 'final'

    /**
     * El checksum o SHA del archivo (vital para la PoC de GitHub)
     */
    private String sha;         // ◄--- Quitamos el 'final'

    // --- ESPECÍFICO PARA ENRUTAMIENTO O PIP ---
    /**
     * La ruta del archivo o el path destino (Ej: "/api/v1/payments")
     */
    private String targetPath;  // ◄--- Quitamos el 'final'

    // --- GENÉRICO PARA CUALQUIER CONFIGURACIÓN (Key-Value) ---
    /**
     * Parámetros dinámicos en formato Clave-Valor.
     */
    private Map<String, Object> parameters; // ◄--- Quitamos el 'final'
}
