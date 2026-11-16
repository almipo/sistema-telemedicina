package com.telemedicina.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.*;
@Configuration public class OpenApiConfig {
 @Bean public OpenAPI telemedicinaApi(){return new OpenAPI().info(new Info().title("Telemedicina - API Backend").version("1.0.0").description("API transaccional de pacientes, medicos, turnos, dispositivos e historias clinicas"));}
}
