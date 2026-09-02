package com.smarthas.api.web;

import com.smarthas.api.dto.HealthUnitRequest;
import com.smarthas.api.dto.HealthUnitResponse;
import com.smarthas.api.service.HealthUnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Unidades de Saude", description = "Pontos do mapa (hospitais, sensores IoT). Escrita restrita a ADMIN")
@RestController
@RequestMapping("/units")
public class HealthUnitController {

    private final HealthUnitService service;

    public HealthUnitController(HealthUnitService service) {
        this.service = service;
    }

    @Operation(summary = "Lista todas as unidades (publico)")
    @GetMapping
    public List<HealthUnitResponse> list() {
        return service.list().stream().map(HealthUnitResponse::from).toList();
    }

    @Operation(summary = "Cria uma unidade (somente ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public HealthUnitResponse create(@Valid @RequestBody HealthUnitRequest request) {
        return HealthUnitResponse.from(service.create(request));
    }

    @Operation(summary = "Atualiza uma unidade (somente ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public HealthUnitResponse update(@PathVariable Long id, @Valid @RequestBody HealthUnitRequest request) {
        return HealthUnitResponse.from(service.update(id, request));
    }

    @Operation(summary = "Remove uma unidade (somente ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
