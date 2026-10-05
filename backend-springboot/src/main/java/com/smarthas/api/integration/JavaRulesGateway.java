package com.smarthas.api.integration;

import com.smarthas.api.domain.*;
import com.smarthas.api.repository.AlertRepository;
import com.smarthas.api.repository.MeasurementRepository;
import com.smarthas.api.repository.SummaryReportRepository;
import com.smarthas.api.repository.UserRepository;
import com.smarthas.api.web.ApiException;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Adaptador de contingencia (perfil padrao, banco H2).
 * Reproduz em Java exatamente as regras das procedures/functions PL/SQL,
 * para que o sistema rode e seja testado sem acesso ao Oracle.
 */
@Component
@Profile("!oracle")
public class JavaRulesGateway implements ClinicalRulesGateway {

    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final MeasurementRepository measurements;
    private final AlertRepository alerts;
    private final SummaryReportRepository reports;
    private final UserRepository users;

    public JavaRulesGateway(MeasurementRepository measurements, AlertRepository alerts,
                            SummaryReportRepository reports, UserRepository users) {
        this.measurements = measurements;
        this.alerts = alerts;
        this.reports = reports;
        this.users = users;
    }

    /** Espelho de PRC_SHAS_REGISTRAR_ALERTA. */
    @Override
    @Transactional
    public AlertOutcome registerAlert(Long measurementId) {
        Measurement m = measurements.findById(measurementId)
                .orElseThrow(() -> new ApiException("Medicao " + measurementId + " nao encontrada.", HttpStatus.NOT_FOUND));
        int sis = m.getSystolic();
        int dia = m.getDiastolic();
        Long userId = m.getUser().getId();

        String type;
        String severity;
        String message;
        if (sis >= 180 || dia >= 120) {
            type = "CRISE_HIPERTENSIVA";
            severity = "CRITICO";
            message = "Leitura de " + sis + "x" + dia
                    + " mmHg compativel com crise hipertensiva. Procure atendimento imediato.";
        } else if (sis >= 140 || dia >= 90) {
            long hyper = measurements
                    .findTop5ByUserIdAndMeasuredAtLessThanEqualOrderByMeasuredAtDesc(userId, m.getMeasuredAt())
                    .stream().filter(x -> x.getClassification() == Classification.HYPERTENSION).count();
            if (hyper >= 3) {
                type = "TENDENCIA_ALTA";
                severity = "ALTO";
                message = hyper + " das ultimas 5 leituras indicam hipertensao (ultima: "
                        + sis + "x" + dia + " mmHg). Agende avaliacao medica.";
            } else {
                type = "HIPERTENSAO";
                severity = "MODERADO";
                message = "Leitura de " + sis + "x" + dia
                        + " mmHg acima da meta (140x90). Repita a medicao em repouso.";
            }
        } else {
            return AlertOutcome.none();
        }

        Alert alert = alerts.findByMeasurementIdAndType(measurementId, type)   // idempotencia
                .orElseGet(() -> alerts.save(new Alert(userId, measurementId, type, severity, message)));
        return new AlertOutcome(alert.getId(), severity);
    }

    /** Espelho de PRC_SHAS_GERAR_RELATORIO_RESUMO. */
    @Override
    @Transactional
    public int generateSummaryReports(int days, Long userId) {
        validateDays(days, 365);
        int generated = 0;
        for (User u : users.findAll()) {
            if (u.getRole() != Role.USER || (userId != null && !userId.equals(u.getId()))) continue;

            List<Measurement> period = inPeriod(u.getId(), days);
            int qty = period.size();
            Double avgSis = qty == 0 ? null : round1(period.stream().mapToInt(Measurement::getSystolic).average().orElse(0));
            Double avgDia = qty == 0 ? null : round1(period.stream().mapToInt(Measurement::getDiastolic).average().orElse(0));
            Integer maxSis = qty == 0 ? null : period.stream().mapToInt(Measurement::getSystolic).max().orElse(0);
            Double rate = controlRate(u.getId(), days);
            int open = (int) alerts.countByUserIdAndStatus(u.getId(), Alert.STATUS_OPEN);
            long critical = alerts.countByUserIdAndStatusAndSeverity(u.getId(), Alert.STATUS_OPEN, "CRITICO");

            String risk;
            if (qty == 0) risk = "SEM DADOS";
            else if (critical > 0 || rate < 50) risk = "ALTO";
            else if (rate < 80) risk = "MODERADO";
            else risk = "BAIXO";

            reports.save(new SummaryReport(u.getId(), LocalDate.now().minusDays(days), LocalDate.now(),
                    qty, avgSis, avgDia, maxSis, rate, open, risk, truncate(patientSummary(u.getId(), days), 400)));
            generated++;
        }
        return generated;
    }

    /** Espelho de FN_SHAS_TAXA_CONTROLE. */
    @Override
    public Double controlRate(Long userId, int days) {
        validateDays(days, Integer.MAX_VALUE);
        ensureUser(userId);
        List<Measurement> period = inPeriod(userId, days);
        if (period.isEmpty()) return null;
        long ok = period.stream().filter(m -> m.getSystolic() < 140 && m.getDiastolic() < 90).count();
        return round1(ok * 100.0 / period.size());
    }

    /** Espelho de FN_SHAS_RESUMO_PACIENTE. */
    @Override
    public String patientSummary(Long userId, int days) {
        validateDays(days, Integer.MAX_VALUE);
        User u = ensureUser(userId);
        List<Measurement> period = inPeriod(userId, days);
        if (period.isEmpty()) {
            return u.getFullName() + " | nenhuma leitura nos ultimos " + days + " dias";
        }
        long avgSis = Math.round(period.stream().mapToInt(Measurement::getSystolic).average().orElse(0));
        long avgDia = Math.round(period.stream().mapToInt(Measurement::getDiastolic).average().orElse(0));
        Measurement last = measurements.findByUserIdOrderByMeasuredAtDescIdDesc(userId).get(0);
        String rate = String.format(Locale.forLanguageTag("pt-BR"), "%.1f", controlRate(userId, days));
        return u.getFullName()
                + " | " + period.size() + " leituras em " + days + " dias"
                + " | media " + avgSis + "/" + avgDia + " mmHg"
                + " | " + rate + "% na meta"
                + " | ultima: " + last.getMeasuredAt().format(BR) + " (" + last.getClassification().getLabel() + ")";
    }

    @Override
    public List<PatientIndicator> patientIndicators(int days) {
        List<PatientIndicator> list = new ArrayList<>();
        for (User u : users.findAll()) {
            if (u.getRole() != Role.USER) continue;
            list.add(new PatientIndicator(u.getId(), u.getFullName(),
                    controlRate(u.getId(), days), patientSummary(u.getId(), days)));
        }
        Comparator<PatientIndicator> byRate = Comparator.comparing(PatientIndicator::controlRate,
                Comparator.nullsLast(Comparator.<Double>naturalOrder()));
        list.sort(byRate.thenComparing(PatientIndicator::fullName));
        return list;
    }

    @Override
    public String engine() {
        return "JAVA_FALLBACK";
    }

    // ---------- apoio ----------

    private List<Measurement> inPeriod(Long userId, int days) {
        return measurements.findByUserIdAndMeasuredAtGreaterThanEqual(userId, LocalDateTime.now().minusDays(days));
    }

    private User ensureUser(Long userId) {
        return users.findById(userId)
                .orElseThrow(() -> new ApiException("Usuario " + userId + " nao encontrado.", HttpStatus.NOT_FOUND));
    }

    private static void validateDays(int days, int max) {
        if (days <= 0 || days > max) {
            throw new ApiException("Periodo invalido: informe entre 1 e " + Math.min(max, 365) + " dias.",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private static Double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private static String truncate(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max);
    }
}
