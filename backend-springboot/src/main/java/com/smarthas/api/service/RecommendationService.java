package com.smarthas.api.service;

import com.smarthas.api.domain.Classification;
import com.smarthas.api.domain.HealthUnit;
import com.smarthas.api.domain.Measurement;
import com.smarthas.api.domain.User;
import com.smarthas.api.dto.HealthUnitResponse;
import com.smarthas.api.dto.RecommendationResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * "AI Logistics Extension" / apoio a decisao.
 * A partir do historico de medicoes, calcula o nivel de risco, gera recomendacoes
 * e sugere a unidade de saude ativa mais proxima (roteamento logistico simples).
 */
@Service
public class RecommendationService {

    public RecommendationResponse compute(User user,
                                          List<Measurement> measurements,
                                          List<HealthUnit> units,
                                          double refLat, double refLng) {

        int total = measurements.size();
        long normal = measurements.stream().filter(m -> m.getClassification() == Classification.NORMAL).count();
        long elevated = measurements.stream().filter(m -> m.getClassification() == Classification.ELEVATED).count();
        long hyper = measurements.stream().filter(m -> m.getClassification() == Classification.HYPERTENSION).count();

        // Considera as 5 medicoes mais recentes para o nivel de risco (a lista ja vem ordenada desc.)
        List<Measurement> recent = measurements.subList(0, Math.min(5, total));
        long recentHyper = recent.stream().filter(m -> m.getClassification() == Classification.HYPERTENSION).count();

        String riskLevel;
        List<String> recs = new ArrayList<>();

        if (total == 0) {
            riskLevel = "SEM DADOS";
            recs.add("Registre sua primeira medicao para receber recomendacoes personalizadas.");
        } else if (recentHyper >= 3) {
            riskLevel = "ALTO";
            recs.add("Varias medicoes recentes indicam hipertensao. Procure um medico o quanto antes.");
            recs.add("Evite sal em excesso, cafeina e bebidas alcoolicas nas proximas 24h.");
            recs.add("Mantenha a medicao diaria e anote sintomas como dor de cabeca ou tontura.");
        } else if (recentHyper >= 1 || elevated > normal) {
            riskLevel = "MODERADO";
            recs.add("Ha sinais de pressao elevada. Reforce a rotina de medicoes.");
            recs.add("Reduza o consumo de sal e pratique atividade fisica leve regularmente.");
        } else {
            riskLevel = "BAIXO";
            recs.add("Seu quadro esta estavel. Continue monitorando periodicamente.");
        }

        HealthUnit nearest = findNearestActive(units, refLat, refLng);
        if (nearest != null && !"BAIXO".equals(riskLevel) && !"SEM DADOS".equals(riskLevel)) {
            recs.add("Unidade de saude mais proxima sugerida: " + nearest.getName() + ".");
        }

        return new RecommendationResponse(
                total, normal, elevated, hyper, riskLevel, recs,
                nearest == null ? null : HealthUnitResponse.from(nearest)
        );
    }

    private HealthUnit findNearestActive(List<HealthUnit> units, double lat, double lng) {
        HealthUnit best = null;
        double bestDist = Double.MAX_VALUE;
        for (HealthUnit u : units) {
            if (!u.isActive()) continue;
            double d = haversineKm(lat, lng, u.getLatitude(), u.getLongitude());
            if (d < bestDist) {
                bestDist = d;
                best = u;
            }
        }
        return best;
    }

    /** Distancia em km entre dois pontos (formula de Haversine). */
    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
