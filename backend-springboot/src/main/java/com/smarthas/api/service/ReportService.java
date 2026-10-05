package com.smarthas.api.service;

import com.smarthas.api.domain.SummaryReport;
import com.smarthas.api.domain.User;
import com.smarthas.api.dto.ReportRunResponse;
import com.smarthas.api.dto.SummaryReportResponse;
import com.smarthas.api.integration.ClinicalRulesGateway;
import com.smarthas.api.integration.PatientIndicator;
import com.smarthas.api.repository.SummaryReportRepository;
import com.smarthas.api.repository.UserRepository;
import com.smarthas.api.web.ApiException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Orquestra a geracao e a leitura dos relatorios consolidados. */
@Service
public class ReportService {

    private final ClinicalRulesGateway gateway;
    private final SummaryReportRepository reports;
    private final UserRepository users;

    public ReportService(ClinicalRulesGateway gateway, SummaryReportRepository reports, UserRepository users) {
        this.gateway = gateway;
        this.reports = reports;
        this.users = users;
    }

    /** Dispara a procedure de relatorio e devolve os registros recem-gerados. */
    public ReportRunResponse generate(int days, Long userId) {
        if (days < 1 || days > 365) {
            throw new ApiException("Periodo invalido: informe entre 1 e 365 dias.", HttpStatus.BAD_REQUEST);
        }
        long start = System.currentTimeMillis();
        int generated = gateway.generateSummaryReports(days, userId);
        long elapsed = System.currentTimeMillis() - start;
        return new ReportRunResponse(gateway.engine(), days, generated, elapsed, latest(generated));
    }

    public List<SummaryReportResponse> latest(int limit) {
        if (limit <= 0) return List.of();
        List<SummaryReport> list = reports.findAllByOrderByIdDesc(PageRequest.of(0, Math.min(limit, 200)));
        Map<Long, String> names = users.findAllById(list.stream().map(SummaryReport::getUserId).toList())
                .stream().collect(Collectors.toMap(User::getId, User::getFullName, (a, b) -> a));
        return list.stream()
                .map(r -> SummaryReportResponse.from(r, names.getOrDefault(r.getUserId(), "-")))
                .toList();
    }

    public List<PatientIndicator> indicators(int days) {
        return gateway.patientIndicators(days);
    }
}
