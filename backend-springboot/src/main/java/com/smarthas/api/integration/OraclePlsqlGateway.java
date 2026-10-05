package com.smarthas.api.integration;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.List;
import java.util.Map;

/**
 * Adaptador Oracle: REST -> Java -> JDBC -> PL/SQL.
 *
 * Procedures sao chamadas com SimpleJdbcCall (CallableStatement com parametros IN/OUT).
 * Functions sao usadas dentro de consultas SQL comuns, como faria qualquer relatorio.
 * Os parametros sao declarados explicitamente (sem leitura de metadados), o que deixa
 * a chamada mais rapida e independente de sinonimos/permissoes do dicionario Oracle.
 */
@Component
@Profile("oracle")
public class OraclePlsqlGateway implements ClinicalRulesGateway {

    private final JdbcTemplate jdbc;
    private final SimpleJdbcCall registerAlertCall;
    private final SimpleJdbcCall summaryReportCall;

    public OraclePlsqlGateway(JdbcTemplate jdbc) {
        this.jdbc = jdbc;

        this.registerAlertCall = new SimpleJdbcCall(jdbc)
                .withProcedureName("PRC_SHAS_REGISTRAR_ALERTA")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("P_ID_MEDICAO", Types.NUMERIC),
                        new SqlOutParameter("P_ID_ALERTA", Types.NUMERIC),
                        new SqlOutParameter("P_SEVERIDADE", Types.VARCHAR));

        this.summaryReportCall = new SimpleJdbcCall(jdbc)
                .withProcedureName("PRC_SHAS_GERAR_RELATORIO_RESUMO")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("P_DIAS", Types.NUMERIC),
                        new SqlParameter("P_ID_USUARIO", Types.NUMERIC),
                        new SqlOutParameter("P_QT_GERADOS", Types.NUMERIC));
    }

    @Override
    public AlertOutcome registerAlert(Long measurementId) {
        Map<String, Object> out = registerAlertCall.execute(
                new MapSqlParameterSource().addValue("P_ID_MEDICAO", measurementId));
        Number alertId = (Number) value(out, "P_ID_ALERTA");
        String severity = (String) value(out, "P_SEVERIDADE");
        return new AlertOutcome(alertId == null ? null : alertId.longValue(),
                                severity == null ? "NENHUM" : severity);
    }

    @Override
    public int generateSummaryReports(int days, Long userId) {
        Map<String, Object> out = summaryReportCall.execute(new MapSqlParameterSource()
                .addValue("P_DIAS", days)
                .addValue("P_ID_USUARIO", userId, Types.NUMERIC));
        Number qty = (Number) value(out, "P_QT_GERADOS");
        return qty == null ? 0 : qty.intValue();
    }

    @Override
    public Double controlRate(Long userId, int days) {
        BigDecimal rate = jdbc.queryForObject(
                "SELECT FN_SHAS_TAXA_CONTROLE(?, ?) FROM DUAL", BigDecimal.class, userId, days);
        return rate == null ? null : rate.doubleValue();
    }

    @Override
    public String patientSummary(Long userId, int days) {
        return jdbc.queryForObject(
                "SELECT FN_SHAS_RESUMO_PACIENTE(?, ?) FROM DUAL", String.class, userId, days);
    }

    @Override
    public List<PatientIndicator> patientIndicators(int days) {
        String sql = """
                SELECT u.ID_USUARIO,
                       u.NM_COMPLETO,
                       FN_SHAS_TAXA_CONTROLE(u.ID_USUARIO, ?)   AS PC_NA_META,
                       FN_SHAS_RESUMO_PACIENTE(u.ID_USUARIO, ?) AS RESUMO
                  FROM T_SHAS_USUARIO u
                 WHERE u.TP_PERFIL = 'USER'
                 ORDER BY PC_NA_META NULLS LAST, u.NM_COMPLETO
                """;
        return jdbc.query(sql, (rs, i) -> {
            BigDecimal rate = rs.getBigDecimal("PC_NA_META");
            return new PatientIndicator(rs.getLong("ID_USUARIO"), rs.getString("NM_COMPLETO"),
                    rate == null ? null : rate.doubleValue(), rs.getString("RESUMO"));
        }, days, days);
    }

    @Override
    public String engine() {
        return "ORACLE_PLSQL";
    }

    /** Busca no mapa de saida ignorando maiusculas/minusculas (driver pode variar). */
    private static Object value(Map<String, Object> out, String key) {
        for (Map.Entry<String, Object> e : out.entrySet()) {
            if (e.getKey().equalsIgnoreCase(key)) {
                return e.getValue();
            }
        }
        return null;
    }
}
