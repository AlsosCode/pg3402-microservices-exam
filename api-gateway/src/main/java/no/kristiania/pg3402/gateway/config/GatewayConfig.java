package no.kristiania.pg3402.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Catalog Service routes (with load balancing across multiple instances)
                .route("catalog-service", r -> r
                        .path("/api/catalog/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .addRequestHeader("X-Gateway", "API-Gateway"))
                        .uri("lb://catalog-service"))

                // Collection Service routes
                .route("collection-service", r -> r
                        .path("/api/collections/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway", "API-Gateway"))
                        .uri("http://collection-service:8082"))

                // Media Service routes
                .route("media-service", r -> r
                        .path("/api/media/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .addRequestHeader("X-Gateway", "API-Gateway"))
                        .uri("http://media-service:8084"))

                .build();
    }
}
