package co.com.bancolombia.model.policymodel.configurationrule;

public enum RuleType {
    /**
     * Reglas puras de OPA (.rego) para evaluar decisiones de autorización (Permitido/No Permitido).
     */
    REGO_POLICY,

    /**
     * Reglas para el "Traductor de Direcciones" (Enrutamiento).
     * Le dice al PEP a qué API interna enviar la petición del cliente.
     */
    ROUTING,

    /**
     * Configuración para el PIP (Policy Information Point).
     * Define qué datos externos ir a buscar (bases de datos, APIs externas) y con qué credenciales.
     */
    PIP_CONFIG,

    /**
     * Configuración de "Acciones Post" a aplicar al final de la transacción (ej. enviar SMS, email).
     */
    POST_ACTION
}
