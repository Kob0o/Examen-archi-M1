package com.example.member_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI memberServiceOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Member Service — Coworking")
						.description("CRUD des membres, abonnements et suspension (étape 6 : springdoc-openapi).")
						.version("1.0")
						.license(new License().name("Examen M1").url("https://springdoc.org")));
	}
}
