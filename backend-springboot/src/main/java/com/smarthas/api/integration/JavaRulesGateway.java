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
 * Reproduz em Java exatamente as regras dos packages PL/SQL (PKG_SHAS_*),
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

    /** Espelho de PKG_SHAS_ALERTAS.PRC_REGISTRAR_ALERTA. */
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

    /** Espelho de PKG_SHAS_RELATORIOS.PRC_GERAR_RELATORIO_RESUMO. */
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

    /** Espelho de PKG_SHAS_INDICADORES.FN_TAXA_CONTROLE. */
    @Override
    public Double controlRate(Long userId, int days) {
        validateDays(days, Integer.MAX_VALUE);
        ensureUser(userId);
        List<Measurement> period = inPeriod(userId, days);
        if (period.isEmpty()) return null;
        long ok = period.stream().filter(m -> m.getSystolic() < 140 && m.getDiastolic() < 90).count();
        return round1(ok * 100.0 / period.size());
    }

    /** Espelho de PKG_SHAS_INDICADORES.FN_RESUMO_PACIENTE. */
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

    /** Espelho de PKG_SHAS_INDICADORES.PRC_CONSULTAR_MEDICOES (mesmas validacoes e whitelist). */
    @Override
    @Transactional(readOnly = true)
    public MeasurementSearchResult searchMeasurements(MeasurementSearchFilter f) {
        String classification = upper(f.classification());
        String source = upper(f.source());
        if (classification != null && !Set.of("NORMAL", "ELEVATED", "HYPERTENSION").contains(classification)) {
            throw badRequest("Classificacao invalida: use NORMAL, ELEVATED ou HYPERTENSION.");
        }
        if (source != null && !Set.of("MANUAL", "SENSOR").contains(source)) {
            throw badRequest("Origem invalida: use MANUAL ou SENSOR.");
        }
        if (f.days() != null) validateDays(f.days(), Integer.MAX_VALUE);
        if (f.limit() < 1 || f.limit() > 500) throw badRequest("Limite deve estar entre 1 e 500.");

        String orderBy = f.orderBy() == null ? "DATA" : f.orderBy().toUpperCase();
        String direction = f.direction() == null ? "DESC" : f.direction().toUpperCase();
        Comparator<MeasurementSearchRow> order = switch (orderBy) {
            case "DATA" -> Comparator.comparing(MeasurementSearchRow::measuredAt);
            case "SISTOLICA" -> Comparator.comparingInt(MeasurementSearchRow::systolic);
            case "DIASTOLICA" -> Comparator.comparingInt(MeasurementSearchRow::diastolic);
            case "PACIENTE" -> Comparator.comparing(MeasurementSearchRow::patientName);
            default -> null;
        };
        if (order == null || !Set.of("ASC", "DESC").contains(direction)) {
            throw badRequest("Ordenacao invalida: campo DATA/SISTOLICA/DIASTOLICA/PACIENTE e direcao ASC/DESC.");
        }
        if (direction.equals("DESC")) order = order.reversed();
        order = order.thenComparing(MeasurementSearchRow::id, Comparator.reverseOrder());

        LocalDateTime from = f.days() == null ? null : LocalDateTime.now().minusDays(f.days());
        List<MeasurementSearchRow> matches = measurements.findAll().stream()
                .filter(m -> f.userId() == null || f.userId().equals(m.getUser().getId()))
                .filter(m -> classification == null || m.getClassification().name().equals(classification))
                .filter(m -> source == null || source.equals(m.getSource()))
                .filter(m -> from == null || !m.getMeasuredAt().isBefore(from))
                .filter(m -> f.minSystolic() == null || m.getSystolic() >= f.minSystolic())
                .map(m -> new MeasurementSearchRow(m.getId(), m.getUser().getId(), m.getUser().getFullName(),
                        m.getSystolic(), m.getDiastolic(), m.getHeartRate(), m.getMeasuredAt(), m.getSource(),
                        m.getClassification().name()))
                .sorted(order)
                .toList();

        String description = "Filtro em Java (perfil H2): usuario=" + star(f.userId()) + " classif=" + star(classification)
                + " origem=" + star(source) + " dias=" + star(f.days()) + " sis_min=" + star(f.minSystolic())
                + " ordem=" + orderBy + " " + direction + " limite=" + f.limit();
        return new MeasurementSearchResult(engine(), matches.size(), description,
                matches.subList(0, Math.min(f.limit(), matches.size())));
    }

    /** Espelho de PKG_SHAS_INDICADORES.FN_ESTATISTICA_POR_DIMENSAO. */
    @Override
    @Transactional(readOnly = true)
    public List<DimensionStat> statsByDimension(String dimension, int days) {
        validateDays(days, Integer.MAX_VALUE);
        String dim = dimension == null ? "" : dimension.trim().toUpperCase();
        java.util.function.Function<Measurement, String> key = switch (dim) {
            case "CLASSIFICACAO" -> m -> m.getClassification().getLabel();
            case "ORIGEM" -> Measurement::getSource;
            case "PACIENTE" -> m -> m.getUser().getFullName();
            case "PERIODO_DIA" -> m -> m.getMeasuredAt().getHour() < 12 ? "Manha"
                    : m.getMeasuredAt().getHour() < 18 ? "Tarde" : "Noite";
            default -> throw badRequest("Dimensao invalida: use CLASSIFICACAO, ORIGEM, PACIENTE ou PERIODO_DIA.");
        };
        LocalDateTime from = LocalDateTime.now().minusDays(days);
        Map<String, List<Measurement>> groups = new TreeMap<>();
        measurements.findAll().stream()
                .filter(m -> !m.getMeasuredAt().isBefore(from))
                .forEach(m -> groups.computeIfAbsent(key.apply(m), k -> new ArrayList<>()).add(m));

        List<DimensionStat> stats = new ArrayList<>();
        groups.forEach((group, list) -> {
            long ok = list.stream().filter(m -> m.getSystolic() < 140 && m.getDiastolic() < 90).count();
            stats.add(new DimensionStat(group, list.size(),
                    round1(list.stream().mapToInt(Measurement::getSystolic).average().orElse(0)),
                    round1(list.stream().mapToInt(Measurement::getDiastolic).average().orElse(0)),
                    round1(ok * 100.0 / list.size())));
        });
        stats.sort(Comparator.comparingLong(DimensionStat::measurements).reversed()
                .thenComparing(DimensionStat::group));
        return stats;
    }

    /** Espelho de PKG_SHAS_ALERTAS.PRC_RESOLVER_ALERTAS_PACIENTE. */
    @Override
    @Transactional
    public int resolvePatientAlerts(Long userId) {
        ensureUser(userId);
        List<Alert> open = alerts.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(a -> Alert.STATUS_OPEN.equals(a.getStatus()))
                .toList();
        open.forEach(Alert::resolve);
        alerts.saveAll(open);
        return open.size();
    }

    /** A trilha de auditoria e gravada por trigger Oracle; no H2 nao ha triggers. */
    @Override
    public List<AlertAuditEntry> alertAudit(Long alertId) {
        return List.of();
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

    private static ApiException badRequest(String message) {
        return new ApiException(message, HttpStatus.BAD_REQUEST);
    }

    private static String upper(String s) {
        return s == null || s.isBlank() ? null : s.trim().toUpperCase();
    }

    private static String star(Object v) {
        return v == null ? "*" : v.toString();
    }

    private static Double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private static String truncate(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max);
    }
}
