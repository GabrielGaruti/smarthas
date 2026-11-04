package com.smarthas.api.integration;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adaptador Oracle: REST -> Java -> JDBC -> PL/SQL.
 *
 * Fase 7: as rotinas foram agrupadas em packages; o SimpleJdbcCall usa o nome do package
 * como "catalog" ({call PKG_SHAS_ALERTAS.PRC_REGISTRAR_ALERTA(?, ?, ?)}).
 * Procedures sao chamadas com SimpleJdbcCall (CallableStatement com parametros IN/OUT).
 * Functions escalares sao usadas dentro de consultas SQL comuns; as rotinas de SQL dinamico
 * devolvem um REF CURSOR, lido como ResultSet.
 * Os parametros sao declarados explicitamente (sem leitura de metadados), o que deixa
 * a chamada mais rapida e independente de sinonimos/permissoes do dicionario Oracle.
 */
@Component
@Profile("oracle")
public class OraclePlsqlGateway implements ClinicalRulesGateway {

    /** oracle.jdbc.OracleTypes.CURSOR (o driver so esta no classpath em runtime). */
    private static final int ORACLE_CURSOR = -10;

    private static final RowMapper<MeasurementSearchRow> SEARCH_ROW = (rs, i) -> {
        Timestamp at = rs.getTimestamp("DT_MEDICAO");
        int hr = rs.getInt("VL_FREQ_CARDIACA");
        return new MeasurementSearchRow(rs.getLong("ID_MEDICAO"), rs.getLong("ID_USUARIO"),
                rs.getString("NM_COMPLETO"), rs.getInt("VL_SISTOLICA"), rs.getInt("VL_DIASTOLICA"),
                rs.wasNull() ? null : hr, at == null ? null : at.toLocalDateTime(),
                rs.getString("TP_ORIGEM"), rs.getString("CD_CLASSIFICACAO"));
    };

    private final JdbcTemplate jdbc;
    private final SimpleJdbcCall registerAlertCall;
    private final SimpleJdbcCall summaryReportCall;
    private final SimpleJdbcCall searchCall;
    private final SimpleJdbcCall resolveAlertsCall;

    public OraclePlsqlGateway(JdbcTemplate jdbc) {
        this.jdbc = jdbc;

        this.registerAlertCall = new SimpleJdbcCall(jdbc)
                .withCatalogName("PKG_SHAS_ALERTAS")
                .withProcedureName("PRC_REGISTRAR_ALERTA")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("P_ID_MEDICAO", Types.NUMERIC),
                        new SqlOutParameter("P_ID_ALERTA", Types.NUMERIC),
                        new SqlOutParameter("P_SEVERIDADE", Types.VARCHAR));

        this.summaryReportCall = new SimpleJdbcCall(jdbc)
                .withCatalogName("PKG_SHAS_RELATORIOS")
                .withProcedureName("PRC_GERAR_RELATORIO_RESUMO")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("P_DIAS", Types.NUMERIC),
                        new SqlParameter("P_ID_USUARIO", Types.NUMERIC),
                        new SqlOutParameter("P_QT_GERADOS", Types.NUMERIC));

        this.searchCall = new SimpleJdbcCall(jdbc)
                .withCatalogName("PKG_SHAS_INDICADORES")
                .withProcedureName("PRC_CONSULTAR_MEDICOES")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("P_ID_USUARIO", Types.NUMERIC),
                        new SqlParameter("P_CLASSIFICACAO", Types.VARCHAR),
                        new SqlParameter("P_ORIGEM", Types.VARCHAR),
                        new SqlParameter("P_DIAS", Types.NUMERIC),
                        new SqlParameter("P_SISTOLICA_MIN", Types.NUMERIC),
                        new SqlParameter("P_ORDENAR_POR", Types.VARCHAR),
                        new SqlParameter("P_DIRECAO", Types.VARCHAR),
                        new SqlParameter("P_LIMITE", Types.NUMERIC),
                        new SqlOutParameter("P_TOTAL", Types.NUMERIC),
                        new SqlOutParameter("P_SQL", Types.VARCHAR),
                        new SqlOutParameter("P_CURSOR", ORACLE_CURSOR, SEARCH_ROW));

        this.resolveAlertsCall = new SimpleJdbcCall(jdbc)
                .withCatalogName("PKG_SHAS_ALERTAS")
                .withProcedureName("PRC_RESOLVER_ALERTAS_PACIENTE")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("P_ID_USUARIO", Types.NUMERIC),
                        new SqlOutParameter("P_QT_RESOLVIDOS", Types.NUMERIC));
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
                "SELECT PKG_SHAS_INDICADORES.FN_TAXA_CONTROLE(?, ?) FROM DUAL", BigDecimal.class, userId, days);
        return rate == null ? null : rate.doubleValue();
    }

    @Override
    public String patientSummary(Long userId, int days) {
        return jdbc.queryForObject(
                "SELECT PKG_SHAS_INDICADORES.FN_RESUMO_PACIENTE(?, ?) FROM DUAL", String.class, userId, days);
    }

    @Override
    public List<PatientIndicator> patientIndicators(int days) {
        String sql = """
                SELECT u.ID_USUARIO,
                       u.NM_COMPLETO,
                       PKG_SHAS_INDICADORES.FN_TAXA_CONTROLE(u.ID_USUARIO, ?)   AS PC_NA_META,
                       PKG_SHAS_INDICADORES.FN_RESUMO_PACIENTE(u.ID_USUARIO, ?) AS RESUMO
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
    @SuppressWarnings("unchecked")
    public MeasurementSearchResult searchMeasurements(MeasurementSearchFilter f) {
        Map<String, Object> out = searchCall.execute(new MapSqlParameterSource()
                .addValue("P_ID_USUARIO", f.userId(), Types.NUMERIC)
                .addValue("P_CLASSIFICACAO", f.classification(), Types.VARCHAR)
                .addValue("P_ORIGEM", f.source(), Types.VARCHAR)
                .addValue("P_DIAS", f.days(), Types.NUMERIC)
                .addValue("P_SISTOLICA_MIN", f.minSystolic(), Types.NUMERIC)
                .addValue("P_ORDENAR_POR", f.orderBy(), Types.VARCHAR)
                .addValue("P_DIRECAO", f.direction(), Types.VARCHAR)
                .addValue("P_LIMITE", f.limit()));
        Number total = (Number) value(out, "P_TOTAL");
        List<MeasurementSearchRow> rows = (List<MeasurementSearchRow>) value(out, "P_CURSOR");
        return new MeasurementSearchResult(engine(), total == null ? 0 : total.longValue(),
                (String) value(out, "P_SQL"), rows == null ? List.of() : rows);
    }

    /** Function que devolve SYS_REFCURSOR: chamada com a sintaxe JDBC {? = call ...}. */
    @Override
    public List<DimensionStat> statsByDimension(String dimension, int days) {
        return jdbc.execute(
                (CallableStatementCreator) con -> {
                    CallableStatement cs = con.prepareCall(
                            "{? = call PKG_SHAS_INDICADORES.FN_ESTATISTICA_POR_DIMENSAO(?, ?)}");
                    cs.registerOutParameter(1, ORACLE_CURSOR);
                    cs.setString(2, dimension);
                    cs.setInt(3, days);
                    return cs;
                },
                (CallableStatementCallback<List<DimensionStat>>) cs -> {
                    cs.execute();
                    List<DimensionStat> list = new ArrayList<>();
                    try (ResultSet rs = (ResultSet) cs.getObject(1)) {
                        while (rs.next()) {
                            list.add(new DimensionStat(rs.getString("DS_GRUPO"), rs.getLong("QT_MEDICOES"),
                                    decimal(rs, "VL_MEDIA_SISTOLICA"), decimal(rs, "VL_MEDIA_DIASTOLICA"),
                                    decimal(rs, "PC_NA_META")));
                        }
                    }
                    return list;
                });
    }

    @Override
    public int resolvePatientAlerts(Long userId) {
        Map<String, Object> out = resolveAlertsCall.execute(
                new MapSqlParameterSource().addValue("P_ID_USUARIO", userId));
        Number qty = (Number) value(out, "P_QT_RESOLVIDOS");
        return qty == null ? 0 : qty.intValue();
    }

    @Override
    public List<AlertAuditEntry> alertAudit(Long alertId) {
        String sql = """
                SELECT ID_AUDITORIA, ID_ALERTA, TP_OPERACAO, ST_ANTERIOR, ST_NOVO, NV_SEVERIDADE,
                       NM_USUARIO_BD, DS_ORIGEM, DT_OPERACAO
                  FROM T_SHAS_AUDITORIA_ALERTA
                 WHERE ID_ALERTA = ?
                 ORDER BY ID_AUDITORIA
                """;
        return jdbc.query(sql, (rs, i) -> new AlertAuditEntry(rs.getLong("ID_AUDITORIA"), rs.getLong("ID_ALERTA"),
                rs.getString("TP_OPERACAO"), rs.getString("ST_ANTERIOR"), rs.getString("ST_NOVO"),
                rs.getString("NV_SEVERIDADE"), rs.getString("NM_USUARIO_BD"), rs.getString("DS_ORIGEM"),
                rs.getTimestamp("DT_OPERACAO").toLocalDateTime()), alertId);
    }

    @Override
    public String engine() {
        return "ORACLE_PLSQL";
    }

    private static Double decimal(ResultSet rs, String column) throws java.sql.SQLException {
        BigDecimal v = rs.getBigDecimal(column);
        return v == null ? null : v.doubleValue();
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
