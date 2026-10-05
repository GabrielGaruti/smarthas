package com.smarthas.api.web;

import com.smarthas.api.domain.Alert;
import com.smarthas.api.domain.User;
import com.smarthas.api.dto.RecommendationResponse;
import com.smarthas.api.integration.ClinicalRulesGateway;
import com.smarthas.api.repository.AlertRepository;
import com.smarthas.api.security.AppUserDetails;
import com.smarthas.api.service.HealthUnitService;
import com.smarthas.api.service.MeasurementService;
import com.smarthas.api.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Recomendacoes", description = "Apoio a decisao (AI Logistics Extension + functions PL/SQL)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final MeasurementService measurementService;
    private final HealthUnitService healthUnitService;
    private final ClinicalRulesGateway gateway;
    private final AlertRepository alertRepository;

    public RecommendationController(RecommendationService recommendationService,
                                    MeasurementService measurementService,
                                    HealthUnitService healthUnitService,
                                    ClinicalRulesGateway gateway,
                                    AlertRepository alertRepository) {
        this.recommendationService = recommendationService;
        this.measurementService = measurementService;
        this.healthUnitService = healthUnitService;
        this.gateway = gateway;
        this.alertRepository = alertRepository;
    }

    @Operation(summary = "Gera recomendacoes, nivel de risco e indicadores (FN_SHAS_TAXA_CONTROLE / FN_SHAS_RESUMO_PACIENTE)")
    @GetMapping
    public RecommendationResponse recommendations(
            @AuthenticationPrincipal AppUserDetails principal,
            @RequestParam(defaultValue = "-23.5505") double lat,
            @RequestParam(defaultValue = "-46.6333") double lng) {

        User user = principal.getUser();
        return recommendationService.compute(
                user,
                measurementService.listForUser(user),
                healthUnitService.list(),
                lat, lng,
                gateway.controlRate(user.getId(), 30),
                gateway.patientSummary(user.getId(), 30),
                alertRepository.countByUserIdAndStatus(user.getId(), Alert.STATUS_OPEN),
                gateway.engine());
    }
}
