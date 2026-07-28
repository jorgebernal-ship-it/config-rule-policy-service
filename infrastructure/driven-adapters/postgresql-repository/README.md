# PostgreSQL Repository Adapter

## Descripción

Adaptador de persistencia para el almacenamiento de `PolicySet` en PostgreSQL utilizando R2DBC (Reactive Relational Database Connectivity).

## Arquitectura

Este módulo implementa el patrón **Repository** siguiendo los principios de **Clean Architecture**, donde:

- **Gateway/Port**: `PolicySetRepository` (definido en `domain/model`)
- **Adapter**: `PolicySetRepositoryAdapter` (implementación con R2DBC)

## Estructura de Datos

### Modelo de Dominio (Jerarquía)

```
PolicySet
├── id: String
├── name: String
├── channel: String
├── transactionCode: String
├── algorithmCombination: String
├── status: String
├── version: Integer
├── createdAt: Instant
├── updatedAt: Instant
├── createdBy: String
└── policies: List<Policy>
    ├── id: String
    ├── policySetId: String
    ├── name: String
    ├── algorithmCombinationRules: String
    ├── sequence: Integer
    └── rules: List<Rule>
        ├── id: String
        ├── policyId: String
        ├── name: String
        ├── effect: String
        ├── decisionCode: String
        ├── target: Map<String, Object> (JSON)
        ├── whenAttribute: String
        ├── whenOperator: String
        ├── whenValue: String
        └── sequence: Integer
```

### Esquema de Base de Datos

Ver el script SQL completo en: `deployment/postgres-schema.sql`

**Tablas:**
- `policy_set`: Tabla principal
- `policy`: Tabla de políticas (1-N con policy_set)
- `rule`: Tabla de reglas (1-N con policy)

**Relaciones:**
- `policy.policy_set_id` → `policy_set.id` (ON DELETE CASCADE)
- `rule.policy_id` → `policy.id` (ON DELETE CASCADE)

## Configuración

### application.yaml

```yaml
spring:
  r2dbc:
    url: "r2dbc:postgresql://localhost:5432/policy_db"
    username: "postgres"
    password: "postgres"
```

### Dependencias (build.gradle)

```groovy
dependencies {
    implementation project(':model')
    implementation 'org.springframework:spring-context'
    implementation 'org.springframework.boot:spring-boot-starter-data-r2dbc'
    implementation 'org.postgresql:r2dbc-postgresql'
    implementation 'com.fasterxml.jackson.core:jackson-databind'
    implementation 'io.projectreactor:reactor-core'
}
```

## Setup de Base de Datos

### Opción 1: Docker Compose (Recomendado)

Agregar al `compose.yml` del proyecto:

```yaml
services:
  postgres:
    image: postgres:16-alpine
    container_name: policy-postgres
    environment:
      POSTGRES_DB: policy_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./deployment/postgres-schema.sql:/docker-entrypoint-initdb.d/schema.sql

volumes:
  postgres_data:
```

Iniciar:
```bash
docker-compose up -d postgres
```

### Opción 2: PostgreSQL Local

1. Instalar PostgreSQL
2. Crear la base de datos:
```sql
CREATE DATABASE policy_db;
```
3. Ejecutar el schema:
```bash
psql -U postgres -d policy_db -f deployment/postgres-schema.sql
```

## Uso

### Use Cases Disponibles

Los siguientes use cases están configurados en `applications/app-service`:

- `CreatePolicySetUseCase`: Crear un nuevo PolicySet con su jerarquía completa
- `GetPolicySetUseCase`: Obtener por ID o por channel + transactionCode
- `UpdatePolicySetUseCase`: Actualizar un PolicySet existente (incrementa versión)
- `DeletePolicySetUseCase`: Eliminar un PolicySet (cascada a policies y rules)

### API REST

Los endpoints están definidos en `infrastructure/entry-points/reactive-web/api/poc_postgres`:

- `POST /api/policy-sets`: Crear PolicySet
- `GET /api/policy-sets/{id}`: Obtener por ID
- `GET /api/policy-sets/search?channel={channel}&transactionCode={code}`: Buscar
- `PUT /api/policy-sets/{id}`: Actualizar
- `DELETE /api/policy-sets/{id}`: Eliminar

## Características Técnicas

### Transacciones Reactivas

El adaptador utiliza `@Transactional` para garantizar consistencia en operaciones de escritura que afectan múltiples tablas.

### Mapeo JSON

El campo `target` de la entidad `Rule` se persiste como JSONB en PostgreSQL usando:
- `io.r2dbc.postgresql.codec.Json` para el tipo de columna
- Jackson `ObjectMapper` para serialización/deserialización

### Versionado Optimista

Cada `PolicySet` tiene un campo `version` que se incrementa automáticamente en cada actualización.

### Índices

- Índice único en `(channel, transaction_code)` para búsquedas rápidas
- Índices en `sequence` para ordenamiento eficiente
- Índice GIN en columna JSONB `target` para queries JSON

## Testing

### Tests Unitarios

Ubicación: `src/test/java/co/com/bancolombia/postgresql`

Ejecutar:
```bash
./gradlew :postgresql-repository:test
```

### Tests de Integración

Para ejecutar tests de integración con Testcontainers (recomendado):

```java
@Testcontainers
class PolicySetRepositoryIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("policy_db")
        .withInitScript("postgres-schema.sql");
    
    // ... tests
}
```

## Troubleshooting

### Error: "relation 'policy_set' does not exist"

**Solución**: Ejecutar el script `deployment/postgres-schema.sql` en la base de datos.

### Error: "ConnectionFactory not found"

**Solución**: Verificar que la configuración `spring.r2dbc.url` esté correctamente establecida en `application.yaml`.

### Error: "Failed to serialize target"

**Solución**: Verificar que el campo `target` en `Rule` sea un `Map<String, Object>` válido y serializable.

## Próximos Pasos

- [ ] Agregar soporte para paginación en búsquedas
- [ ] Implementar cache con Redis para queries frecuentes
- [ ] Agregar auditoría completa (quién modificó qué y cuándo)
- [ ] Implementar soft-delete en lugar de hard-delete
- [ ] Agregar índices adicionales basados en patrones de uso

## Referencias

- [Spring Data R2DBC](https://spring.io/projects/spring-data-r2dbc)
- [R2DBC PostgreSQL Driver](https://github.com/pgjdbc/r2dbc-postgresql)
- [Project Reactor](https://projectreactor.io/)
