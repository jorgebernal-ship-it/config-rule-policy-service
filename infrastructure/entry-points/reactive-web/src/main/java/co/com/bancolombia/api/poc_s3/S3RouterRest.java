package co.com.bancolombia.api.poc_s3;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class S3RouterRest {

    @Bean
    public RouterFunction<ServerResponse> routerS3Function(S3Handler s3Handler) {
        return route(POST("/api/storages3"), s3Handler::create)
                .andRoute(GET("/api/storages3"), s3Handler::list)
                .andRoute(GET("/api/storages3/file"), s3Handler::download)
                .andRoute(PUT("/api/storages3/file"), s3Handler::update)
                .andRoute(DELETE("/api/storages3/file"), s3Handler::delete);
    }
}
