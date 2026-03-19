package com.example.api_gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

	@Bean
	public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
		return builder.routes()
				.route("room-service", r -> r.path("/api/rooms", "/api/rooms/**")
						.uri("lb://room-service"))
				.route("member-service", r -> r.path("/api/members", "/api/members/**")
						.uri("lb://member-service"))
				.route("reservation-service", r -> r.path("/api/reservations", "/api/reservations/**")
						.uri("lb://reservation-service"))
				.build();
	}
}

