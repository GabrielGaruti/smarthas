package com.smarthas.api.web;

import com.smarthas.api.dto.MeasurementRequest;
import com.smarthas.api.dto.MeasurementResponse;
import com.smarthas.api.security.AppUserDetails;
import com.smarthas.api.service.MeasurementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Medicoes", description = "CRUD das medicoes de pressao do usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/measurements")
public class MeasurementController {

    private final MeasurementService service;

    public MeasurementController(MeasurementService service) {
        this.service = service;
    }

    @Operation(summary = "Lista as medicoes do usuario")
    @GetMapping
    public List<MeasurementResponse> list(@AuthenticationPrincipal AppUserDetails principal) {
        return service.listForUser(principal.getUser()).stream()
                .map(MeasurementResponse::from).toList();
    }

    @Operation(summary = "Busca uma medicao por id")
    @GetMapping("/{id}")
    public MeasurementResponse getById(@PathVariable Long id,
                                       @AuthenticationPrincipal AppUserDetails principal) {
        return MeasurementResponse.from(service.getOwned(id, principal.getUser()));
    }

    @Operation(summary = "Cria uma nova medicao")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public MeasurementResponse create(@Valid @RequestBody MeasurementRequest request,
                                      @AuthenticationPrincipal AppUserDetails principal) {
        return MeasurementResponse.from(service.create(request, principal.getUser()));
    }

    @Operation(summary = "Atualiza uma medicao existente")
    @PutMapping("/{id}")
    public MeasurementResponse update(@PathVariable Long id,
                                      @Valid @RequestBody MeasurementRequest request,
                                      @AuthenticationPrincipal AppUserDetails principal) {
        return MeasurementResponse.from(service.update(id, request, principal.getUser()));
    }

    @Operation(summary = "Remove uma medicao")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id,
                       @AuthenticationPrincipal AppUserDetails principal) {
        service.delete(id, principal.getUser());
    }
}
