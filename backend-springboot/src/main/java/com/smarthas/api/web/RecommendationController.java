package com.smarthas.api.web;

import com.smarthas.api.dto.RecommendationResponse;
import com.smarthas.api.security.AppUserDetails;
import com.smarthas.api.service.HealthUnitService;
import com.smarthas.api.service.MeasurementService;
import com.smarthas.api.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Recomendacoes", description = "Apoio a decisao (AI Logistics Extension)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final MeasurementService measurementService;
    private final HealthUnitService healthUnitService;

    public RecommendationController(RecommendationService recommendationService,
                                    MeasurementService measurementService,
                                    HealthUnitService healthUnitService) {
        this.recommendationService = recommendationService;
        this.measurementService = measurementService;
        this.healthUnitService = healthUnitService;
    }

    @Operation(summary = "Gera recomendacoes e nivel de risco a partir do historico")
    @GetMapping
    public RecommendationResponse recommendations(
            @AuthenticationPrincipal AppUserDetails principal,
            @RequestParam(defaultValue = "-23.5505") double lat,
            @RequestParam(defaultValue = "-46.6333") double lng) {

        return recommendationService.compute(
                principal.getUser(),
                measurementService.listForUser(principal.getUser()),
                healthUnitService.list(),
                lat, lng);
    }
}
