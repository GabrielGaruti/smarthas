package com.smarthas.api.integration;

import java.util.List;

/**
 * Porta de integracao com o "motor de regras clinicas" do Smart HAS.
 *
 * A aplicacao depende apenas desta interface (principio da inversao de dependencia).
 * Ha dois adaptadores, escolhidos pelo perfil do Spring:
 *  - {@link OraclePlsqlGateway}  (perfil "oracle"): executa as procedures e functions
 *    PL/SQL no banco via JDBC (SimpleJdbcCall / JdbcTemplate);
 *  - {@link JavaRulesGateway}    (perfil padrao/H2): mesma regra implementada em Java,
 *    para desenvolvimento e testes sem acesso ao Oracle.
 */
public interface ClinicalRulesGateway {

    /** Avalia uma leitura e registra alerta se necessario (PRC_SHAS_REGISTRAR_ALERTA). */
    AlertOutcome registerAlert(Long measurementId);

    /** Gera relatorios resumidos (PRC_SHAS_GERAR_RELATORIO_RESUMO). userId nulo = todos. */
    int generateSummaryReports(int days, Long userId);

    /** Percentual de leituras na meta (FN_SHAS_TAXA_CONTROLE). Nulo = sem dados. */
    Double controlRate(Long userId, int days);

    /** Texto-resumo do paciente (FN_SHAS_RESUMO_PACIENTE). */
    String patientSummary(Long userId, int days);

    /** Painel de indicadores de todos os pacientes (functions usadas dentro de um SELECT). */
    List<PatientIndicator> patientIndicators(int days);

    /** Identificador do motor em uso: ORACLE_PLSQL ou JAVA_FALLBACK. */
    String engine();
}
