package com.smarthas.api.web;

import com.smarthas.api.dto.AlertResponse;
import com.smarthas.api.security.AppUserDetails;
import com.smarthas.api.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Alertas", description = "Alertas clinicos gerados pela procedure PRC_SHAS_REGISTRAR_ALERTA")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/alerts")
public class AlertController {

    private final AlertService service;

    public AlertController(AlertService service) {
        this.service = service;
    }

    @Operation(summary = "Lista os alertas do usuario autenticado")
    @GetMapping
    public List<AlertResponse> mine(@AuthenticationPrincipal AppUserDetails principal) {
        String name = principal.getUser().getFullName();
        return service.listForUser(principal.getUser()).stream()
                .map(a -> AlertResponse.from(a, name)).toList();
    }

    @Operation(summary = "Marca um alerta como resolvido")
    @PatchMapping("/{id}/resolve")
    public AlertResponse resolve(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails principal) {
        return AlertResponse.from(service.resolve(id, principal.getUser()), null);
    }
}
