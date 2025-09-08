package com.pharma.inventory.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Springdoc OpenAPI 설정
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // API 정보 설정
        Info info = new Info()
                .title("Pharma Inventory API")
                .version("v1.0.0")
                .description("제약 재고 관리 시스템 API 문서");

        // JWT 인증 스키마 설정
        String jwtSchemeName = "bearerAuth"; // 스키마 이름
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP) // HTTP 방식
                        .scheme("bearer") // Bearer 토큰 사용
                        .bearerFormat("JWT")); // 토큰 형식

        // OpenAPI 객체 생성 및 설정
        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}
