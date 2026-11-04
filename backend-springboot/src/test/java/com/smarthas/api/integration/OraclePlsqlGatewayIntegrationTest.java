package com.smarthas.api.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integracao real Java -> JDBC -> Oracle: chama os packages, o SQL dinamico e verifica o
 * efeito das triggers. Roda somente no pipeline (job "oracle-integration"), contra um
 * Oracle Database Free efemero ja instalado com database/oracle/run_all.sql.
 *   mvn verify -Dtest.groups=oracle -Dtest.excludedGroups=none
 */
@Tag("oracle")
@SpringBootTest
@ActiveProfiles("oracle")
@EnabledIfEnvironmentVariable(named = "ORACLE_URL", matches = ".+")
class OraclePlsqlGatewayIntegrationTest {

    @Autowired ClinicalRulesGateway gateway;
    @Autowired JdbcTemplate jdbc;

    @Test
    void usaOMotorOracle() {
        assertThat(gateway.engine()).isEqualTo("ORACLE_PLSQL");
    }

    @Test
    void packagesETriggersEstaoValidos() {
        Integer invalid = jdbc.queryForObject("""
                SELECT COUNT(*) FROM user_objects
                 WHERE (object_name LIKE 'PKG\\_SHAS\\_%' ESCAPE '\\' OR object_name LIKE 'TRG\\_SHAS\\_%' ESCAPE '\\')
                   AND status <> 'VALID'
                """, Integer.class);
        assertThat(invalid).isZero();
    }

    @Test
    void procedureDoPackageGeraAlertaCriticoEATriggerAudita() {
        Long userId = patientId();
        Long measurementId = insertMeasurement(userId, 188, 122);

        AlertOutcome outcome = gateway.registerAlert(measurementId);

        assertThat(outcome.severity()).isEqualTo("CRITICO");
        List<AlertAuditEntry> audit = gateway.alertAudit(outcome.alertId());
        assertThat(audit).extracting(AlertAuditEntry::operation).containsExactly("INSERT");
        assertThat(audit.get(0).origin()).contains("JDBC");
    }

    @Test
    void triggerPreencheDataDeResolucaoEAuditaAAlteracao() {
        Long userId = patientId();
        AlertOutcome outcome = gateway.registerAlert(insertMeasurement(userId, 185, 121));

        int resolved = gateway.resolvePatientAlerts(userId);

        assertThat(resolved).isGreaterThanOrEqualTo(1);
        Integer withDate = jdbc.queryForObject(
                "SELECT COUNT(*) FROM T_SHAS_ALERTA WHERE ID_ALERTA = ? AND DT_RESOLUCAO IS NOT NULL",
                Integer.class, outcome.alertId());
        assertThat(withDate).isEqualTo(1);
        assertThat(gateway.alertAudit(outcome.alertId()))
                .extracting(AlertAuditEntry::operation).containsExactly("INSERT", "UPDATE");
    }

    @Test
    void sqlDinamicoMontaApenasOsFiltrosInformados() {
        MeasurementSearchResult r = gateway.searchMeasurements(new MeasurementSearchFilter(
                null, "HYPERTENSION", null, 30, 140, "SISTOLICA", "DESC", 5));

        assertThat(r.engine()).isEqualTo("ORACLE_PLSQL");
        assertThat(r.generatedQuery()).contains("FN_CLASSIFICAR", ":b_classif", "ORDER BY m.VL_SISTOLICA DESC",
                "FETCH FIRST :b_limite ROWS ONLY");
        assertThat(r.rows()).isNotEmpty().hasSizeLessThanOrEqualTo(5);
        assertThat(r.rows()).allMatch(row -> row.classification().equals("HYPERTENSION") && row.systolic() >= 140);
        assertThat(r.total()).isGreaterThanOrEqualTo(r.rows().size());
    }

    @Test
    void sqlDinamicoRecusaOrdenacaoForaDaWhitelist() {
        assertThatThrownBy(() -> gateway.searchMeasurements(new MeasurementSearchFilter(
                null, null, null, null, null, "1; DROP TABLE T_SHAS_MEDICAO", "DESC", 5)))
                .hasMessageContaining("ORA-20021");
    }

    @Test
    void functionComGroupByDinamicoDevolveRefCursor() {
        List<DimensionStat> stats = gateway.statsByDimension("ORIGEM", 30);
        assertThat(stats).extracting(DimensionStat::group).contains("MANUAL", "SENSOR");
        assertThat(gateway.statsByDimension("PERIODO_DIA", 30)).isNotEmpty();
    }

    @Test
    void functionsEProceduresDeIndicadoresERelatorio() {
        Long userId = patientId();
        assertThat(gateway.controlRate(userId, 30)).isBetween(0.0, 100.0);
        assertThat(gateway.patientSummary(userId, 30)).startsWith("Paciente Demonstracao |");
        assertThat(gateway.patientIndicators(30)).hasSizeGreaterThanOrEqualTo(5);
        assertThat(gateway.generateSummaryReports(30, null)).isGreaterThanOrEqualTo(5);
    }

    private Long patientId() {
        return jdbc.queryForObject("SELECT ID_USUARIO FROM T_SHAS_USUARIO WHERE DS_EMAIL = 'paciente@smarthas.com'",
                Long.class);
    }

    private Long insertMeasurement(Long userId, int sys, int dia) {
        jdbc.update("""
                INSERT INTO T_SHAS_MEDICAO (ID_USUARIO, VL_SISTOLICA, VL_DIASTOLICA, DT_MEDICAO, DS_OBSERVACAO)
                VALUES (?, ?, ?, CAST(SYSTIMESTAMP AS TIMESTAMP), 'teste de integracao do pipeline')
                """, userId, sys, dia);
        return jdbc.queryForObject("SELECT MAX(ID_MEDICAO) FROM T_SHAS_MEDICAO WHERE ID_USUARIO = ?", Long.class, userId);
    }
}
