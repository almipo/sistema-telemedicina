package com.telemedicina.metricas.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class OpenApiConfig {
 @Bean public OpenAPI apiMetricas(){return new OpenAPI().info(new Info().title("Telemedicina - Métricas").version("1.1.0").description("Recepción y consulta de mediciones IoT en MongoDB"));}
}
