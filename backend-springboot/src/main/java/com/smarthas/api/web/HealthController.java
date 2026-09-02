package com.smarthas.api.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Status", description = "Verificacao de saude da API")
@RestController
public class HealthController {

    @Operation(summary = "Healthcheck da API")
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok", "service", "smarthas-api");
    }
}
