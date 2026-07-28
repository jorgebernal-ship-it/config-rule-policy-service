package co.com.bancolombia.api.poc_postgres;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
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
public class PolicySetRouterRest {

    private static final String BASE_PATH = "/api/policy-sets";

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = BASE_PATH,
                    method = RequestMethod.POST,
                    beanClass = PolicySetHandler.class,
                    beanMethod = "createPolicySet",
                    operation = @Operation(
                            operationId = "createPolicySet",
                            summary = "Create new PolicySet",
                            description = "Creates a new PolicySet with its policies and rules hierarchy in PostgreSQL",
                            tags = {"PolicySet - PostgreSQL"},
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(mediaType = "application/json")
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "PolicySet created successfully"),
                                    @ApiResponse(responseCode = "400", description = "Validation failed"),
                                    @ApiResponse(responseCode = "500", description = "Internal server error")
                            }
                    )
            ),
            @RouterOperation(
                    path = BASE_PATH + "/{id}",
                    method = RequestMethod.GET,
                    beanClass = PolicySetHandler.class,
                    beanMethod = "getPolicySetById",
                    operation = @Operation(
                            operationId = "getPolicySetById",
                            summary = "Get PolicySet by ID",
                            description = "Retrieves a PolicySet with its complete hierarchy (policies and rules) by ID",
                            tags = {"PolicySet - PostgreSQL"},
                            parameters = @Parameter(name = "id", in = ParameterIn.PATH, required = true,
                                    description = "PolicySet ID"),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "PolicySet retrieved successfully"),
                                    @ApiResponse(responseCode = "404", description = "PolicySet not found"),
                                    @ApiResponse(responseCode = "500", description = "Internal server error")
                            }
                    )
            ),
            @RouterOperation(
                    path = BASE_PATH + "/search",
                    method = RequestMethod.GET,
                    beanClass = PolicySetHandler.class,
                    beanMethod = "getPolicySetByChannelAndTxCode",
                    operation = @Operation(
                            operationId = "getPolicySetByChannelAndTxCode",
                            summary = "Search PolicySet by channel and transaction code",
                            description = "Retrieves a PolicySet using the indexed channel and transaction code",
                            tags = {"PolicySet - PostgreSQL"},
                            parameters = {
                                    @Parameter(name = "channel", in = ParameterIn.QUERY, required = true,
                                            description = "Channel (e.g., MOBILE, WEB)"),
                                    @Parameter(name = "transactionCode", in = ParameterIn.QUERY, required = true,
                                            description = "Transaction code")
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "PolicySet retrieved successfully"),
                                    @ApiResponse(responseCode = "400", description = "Missing required query parameters"),
                                    @ApiResponse(responseCode = "404", description = "PolicySet not found"),
                                    @ApiResponse(responseCode = "500", description = "Internal server error")
                            }
                    )
            ),
            @RouterOperation(
                    path = BASE_PATH + "/{id}",
                    method = RequestMethod.PUT,
                    beanClass = PolicySetHandler.class,
                    beanMethod = "updatePolicySet",
                    operation = @Operation(
                            operationId = "updatePolicySet",
                            summary = "Update PolicySet",
                            description = "Updates an existing PolicySet. Version is automatically incremented",
                            tags = {"PolicySet - PostgreSQL"},
                            parameters = @Parameter(name = "id", in = ParameterIn.PATH, required = true,
                                    description = "PolicySet ID to update"),
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(mediaType = "application/json")
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "PolicySet updated successfully"),
                                    @ApiResponse(responseCode = "400", description = "Validation failed"),
                                    @ApiResponse(responseCode = "404", description = "PolicySet not found"),
                                    @ApiResponse(responseCode = "500", description = "Internal server error")
                            }
                    )
            ),
            @RouterOperation(
                    path = BASE_PATH + "/{id}",
                    method = RequestMethod.DELETE,
                    beanClass = PolicySetHandler.class,
                    beanMethod = "deletePolicySet",
                    operation = @Operation(
                            operationId = "deletePolicySet",
                            summary = "Delete PolicySet",
                            description = "Permanently deletes a PolicySet and cascades to its policies and rules",
                            tags = {"PolicySet - PostgreSQL"},
                            parameters = @Parameter(name = "id", in = ParameterIn.PATH, required = true,
                                    description = "PolicySet ID to delete"),
                            responses = {
                                    @ApiResponse(responseCode = "204", description = "PolicySet deleted successfully"),
                                    @ApiResponse(responseCode = "404", description = "PolicySet not found"),
                                    @ApiResponse(responseCode = "500", description = "Internal server error")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> policySetRoutes(PolicySetHandler handler) {
        return route(POST(BASE_PATH).and(accept(MediaType.APPLICATION_JSON)), handler::createPolicySet)
                .andRoute(GET(BASE_PATH + "/{id}").and(accept(MediaType.APPLICATION_JSON)), handler::getPolicySetById)
                .andRoute(GET(BASE_PATH + "/search").and(accept(MediaType.APPLICATION_JSON)),
                        handler::getPolicySetByChannelAndTxCode)
                .andRoute(PUT(BASE_PATH + "/{id}").and(accept(MediaType.APPLICATION_JSON)), handler::updatePolicySet)
                .andRoute(DELETE(BASE_PATH + "/{id}"), handler::deletePolicySet);
    }
}
