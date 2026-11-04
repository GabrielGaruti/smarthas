package com.smarthas.api.integration;

import java.util.List;

/**
 * Porta de integracao com o "motor de regras clinicas" do Smart HAS.
 *
 * A aplicacao depende apenas desta interface (principio da inversao de dependencia).
 * Ha dois adaptadores, escolhidos pelo perfil do Spring:
 *  - {@link OraclePlsqlGateway}  (perfil "oracle"): executa os packages PL/SQL
 *    (PKG_SHAS_*) no banco via JDBC (SimpleJdbcCall / JdbcTemplate);
 *  - {@link JavaRulesGateway}    (perfil padrao/H2): mesma regra implementada em Java,
 *    para desenvolvimento e testes sem acesso ao Oracle.
 */
public interface ClinicalRulesGateway {

    /** Avalia uma leitura e registra alerta se necessario (PKG_SHAS_ALERTAS.PRC_REGISTRAR_ALERTA). */
    AlertOutcome registerAlert(Long measurementId);

    /** Gera relatorios resumidos (PKG_SHAS_RELATORIOS.PRC_GERAR_RELATORIO_RESUMO). userId nulo = todos. */
    int generateSummaryReports(int days, Long userId);

    /** Percentual de leituras na meta (PKG_SHAS_INDICADORES.FN_TAXA_CONTROLE). Nulo = sem dados. */
    Double controlRate(Long userId, int days);

    /** Texto-resumo do paciente (PKG_SHAS_INDICADORES.FN_RESUMO_PACIENTE). */
    String patientSummary(Long userId, int days);

    /** Painel de indicadores de todos os pacientes (functions usadas dentro de um SELECT). */
    List<PatientIndicator> patientIndicators(int days);

    /** Busca com filtros combinados em tempo real (SQL dinamico: PRC_CONSULTAR_MEDICOES). */
    MeasurementSearchResult searchMeasurements(MeasurementSearchFilter filter);

    /** Estatistica agrupada pela dimensao pedida (SQL dinamico: FN_ESTATISTICA_POR_DIMENSAO). */
    List<DimensionStat> statsByDimension(String dimension, int days);

    /** Encerra os alertas abertos do paciente (PKG_SHAS_ALERTAS.PRC_RESOLVER_ALERTAS_PACIENTE). */
    int resolvePatientAlerts(Long userId);

    /** Trilha de auditoria de um alerta (T_SHAS_AUDITORIA_ALERTA, gravada por trigger). */
    List<AlertAuditEntry> alertAudit(Long alertId);

    /** Identificador do motor em uso: ORACLE_PLSQL ou JAVA_FALLBACK. */
    String engine();
}
