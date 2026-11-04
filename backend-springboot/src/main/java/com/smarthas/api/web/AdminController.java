package com.smarthas.api.web;

import com.smarthas.api.domain.User;
import com.smarthas.api.dto.AlertResponse;
import com.smarthas.api.dto.ReportRunResponse;
import com.smarthas.api.dto.SummaryReportResponse;
import com.smarthas.api.integration.*;
import com.smarthas.api.repository.UserRepository;
import com.smarthas.api.service.AlertService;
import com.smarthas.api.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Rotas administrativas (perfil ADMIN) consumidas pelo painel Angular.
 * Aqui esta a chamada "REST -> Java -> JDBC -> Oracle" dos packages PL/SQL
 * (relatorio, indicadores, SQL dinamico e auditoria por trigger).
 */
@Tag(name = "Administracao", description = "Relatorios, indicadores e alertas de todos os pacientes")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ReportService reportService;
    private final AlertService alertService;
    private final UserRepository userRepository;

    public AdminController(ReportService reportService, AlertService alertService, UserRepository userRepository) {
        this.reportService = reportService;
        this.alertService = alertService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Executa PKG_SHAS_RELATORIOS.PRC_GERAR_RELATORIO_RESUMO e devolve os relatorios gerados")
    @PostMapping("/reports/summary")
    public ReportRunResponse generateReports(@RequestParam(defaultValue = "30") int days,
                                             @RequestParam(required = false) Long userId) {
        return reportService.generate(days, userId);
    }

    @Operation(summary = "Lista os ultimos relatorios gerados")
    @GetMapping("/reports")
    public List<SummaryReportResponse> reports(@RequestParam(defaultValue = "50") int limit) {
        return reportService.latest(limit);
    }

    @Operation(summary = "Painel de indicadores por paciente (functions PL/SQL dentro de um SELECT)")
    @GetMapping("/patients/indicators")
    public List<PatientIndicator> indicators(@RequestParam(defaultValue = "30") int days) {
        return reportService.indicators(days);
    }

    @Operation(summary = "Busca dinamica de medicoes (PKG_SHAS_INDICADORES.PRC_CONSULTAR_MEDICOES - SQL dinamico)",
               description = "Todos os filtros sao opcionais e combinaveis. classification: NORMAL|ELEVATED|HYPERTENSION; "
                       + "source: MANUAL|SENSOR; orderBy: DATA|SISTOLICA|DIASTOLICA|PACIENTE; direction: ASC|DESC.")
    @GetMapping("/measurements/search")
    public MeasurementSearchResult searchMeasurements(@RequestParam(required = false) Long userId,
                                                      @RequestParam(required = false) String classification,
                                                      @RequestParam(required = false) String source,
                                                      @RequestParam(required = false) Integer days,
                                                      @RequestParam(required = false) Integer minSystolic,
                                                      @RequestParam(defaultValue = "DATA") String orderBy,
                                                      @RequestParam(defaultValue = "DESC") String direction,
                                                      @RequestParam(defaultValue = "50") int limit) {
        return reportService.search(new MeasurementSearchFilter(
                userId, classification, source, days, minSystolic, orderBy, direction, limit));
    }

    @Operation(summary = "Estatistica por dimensao escolhida em tempo real (FN_ESTATISTICA_POR_DIMENSAO - GROUP BY dinamico)",
               description = "dimension: CLASSIFICACAO | ORIGEM | PACIENTE | PERIODO_DIA")
    @GetMapping("/measurements/stats")
    public List<DimensionStat> measurementStats(@RequestParam(defaultValue = "CLASSIFICACAO") String dimension,
                                                @RequestParam(defaultValue = "30") int days) {
        return reportService.stats(dimension, days);
    }

    @Operation(summary = "Resolve todos os alertas abertos de um paciente (PKG_SHAS_ALERTAS.PRC_RESOLVER_ALERTAS_PACIENTE)")
    @PostMapping("/patients/{userId}/alerts/resolve")
    public Map<String, Object> resolvePatientAlerts(@PathVariable Long userId) {
        return Map.of("userId", userId, "resolved", reportService.resolvePatientAlerts(userId));
    }

    @Operation(summary = "Trilha de auditoria de um alerta (T_SHAS_AUDITORIA_ALERTA, gravada pela trigger)")
    @GetMapping("/alerts/{alertId}/audit")
    public List<AlertAuditEntry> alertAudit(@PathVariable Long alertId) {
        return reportService.alertAudit(alertId);
    }

    @Operation(summary = "Lista alertas de todos os pacientes (filtro opcional: ABERTO ou RESOLVIDO)")
    @GetMapping("/alerts")
    public List<AlertResponse> alerts(@RequestParam(required = false) String status) {
        Map<Long, String> names = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, User::getFullName));
        return alertService.listAll(status).stream()
                .map(a -> AlertResponse.from(a, names.getOrDefault(a.getUserId(), "-")))
                .toList();
    }
}
