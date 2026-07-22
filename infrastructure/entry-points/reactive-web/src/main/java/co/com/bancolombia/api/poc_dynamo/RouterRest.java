package co.com.bancolombia.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {

    private static final String BASE_PATH = "/configuration-rules";

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = BASE_PATH,
                    method = RequestMethod.POST,
                    beanClass = ConfigurationRuleHandler.class,
                    beanMethod = "createConfigurationRule",
                    operation = @Operation(
                            operationId = "createConfigurationRule",
                            summary = "Create new configuration rule",
                            description = "Creates a new rule with version=1. Valid types: routing, pip-source, post-action, event, policy-rego",
                            tags = {"Configuration Rules"},
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(mediaType = "application/json")
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Rule created successfully"),
                                    @ApiResponse(responseCode = "400", description = "Validation failed"),
                                    @ApiResponse(responseCode = "500", description = "Internal server error")
                            }
                    )
            ),
            @RouterOperation(
                    path = BASE_PATH + "/{id}",
                    method = RequestMethod.GET,
                    beanClass = ConfigurationRuleHandler.class,
                    beanMethod = "getConfigurationRule",
                    operation = @Operation(
                            operationId = "getConfigurationRule",
                            summary = "Get complete rule",
                            description = "Gets a configuration rule in JSON format with all fields and structured spec",
                            tags = {"Configuration Rules"},
                            parameters = @Parameter(name = "id", in = ParameterIn.PATH, required = true, description = "Rule ID"),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Rule retrieved successfully"),
                                    @ApiResponse(responseCode = "404", description = "Rule not found"),
                                    @ApiResponse(responseCode = "500", description = "Internal server error")
                            }
                    )
            ),
            @RouterOperation(
                    path = BASE_PATH + "/{id}/raw",
                    method = RequestMethod.GET,
                    beanClass = ConfigurationRuleHandler.class,
                    beanMethod = "getConfigurationRuleRaw",
                    operation = @Operation(
                            operationId = "getConfigurationRuleRaw",
                            summary = "Get raw content (policy-rego only)",
                            description = "Gets raw content of a policy-rego rule in plain text format. Only available for policy-rego type",
                            tags = {"Configuration Rules"},
                            parameters = @Parameter(name = "id", in = ParameterIn.PATH, required = true, description = "Rule ID"),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Raw content retrieved successfully",
                                            content = @Content(mediaType = "text/plain")),
                                    @ApiResponse(responseCode = "404", description = "Rule not found or not policy-rego type"),
                                    @ApiResponse(responseCode = "500", description = "Internal server error")
                            }
                    )
            ),
            @RouterOperation(
                    path = BASE_PATH + "/{id}",
                    method = RequestMethod.PUT,
                    beanClass = ConfigurationRuleHandler.class,
                    beanMethod = "updateConfigurationRule",
                    operation = @Operation(
                            operationId = "updateConfigurationRule",
                            summary = "Update existing rule",
                            description = "Updates an existing rule. Version is automatically incremented. Fields id, createdAt and createdBy are preserved",
                            tags = {"Configuration Rules"},
                            parameters = @Parameter(name = "id", in = ParameterIn.PATH, required = true, description = "Rule ID to update"),
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(mediaType = "application/json")
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Rule updated successfully"),
                                    @ApiResponse(responseCode = "400", description = "Validation failed"),
                                    @ApiResponse(responseCode = "404", description = "Rule not found"),
                                    @ApiResponse(responseCode = "500", description = "Internal server error")
                            }
                    )
            ),
            @RouterOperation(
                    path = BASE_PATH + "/{id}",
                    method = RequestMethod.DELETE,
                    beanClass = ConfigurationRuleHandler.class,
                    beanMethod = "deleteConfigurationRule",
                    operation = @Operation(
                            operationId = "deleteConfigurationRule",
                            summary = "Delete rule",
                            description = "Permanently deletes a configuration rule",
                            tags = {"Configuration Rules"},
                            parameters = @Parameter(name = "id", in = ParameterIn.PATH, required = true, description = "Rule ID to delete"),
                            responses = {
                                    @ApiResponse(responseCode = "204", description = "Rule deleted successfully"),
                                    @ApiResponse(responseCode = "404", description = "Rule not found"),
                                    @ApiResponse(responseCode = "500", description = "Internal server error")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> configurationRuleRoutes(ConfigurationRuleHandler handler) {
        return route(POST(BASE_PATH).and(accept(MediaType.APPLICATION_JSON)), handler::createConfigurationRule)
                .andRoute(GET(BASE_PATH + "/{id}").and(accept(MediaType.APPLICATION_JSON)), handler::getConfigurationRule)
                .andRoute(GET(BASE_PATH + "/{id}/raw").and(accept(MediaType.TEXT_PLAIN)), handler::getConfigurationRuleRaw)
                .andRoute(PUT(BASE_PATH + "/{id}").and(accept(MediaType.APPLICATION_JSON)), handler::updateConfigurationRule)
                .andRoute(DELETE(BASE_PATH + "/{id}"), handler::deleteConfigurationRule);
    }
}
