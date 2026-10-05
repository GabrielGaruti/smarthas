-- ============================================================
-- Smart HAS - Fase 6 | 05_testes_e_consultas.sql
-- Demonstra o uso pratico das functions em consultas SQL e a
-- execucao das procedures, incluindo os cenarios de erro.
-- (No SQL Developer: execute como script com F5.)
-- ============================================================
SET SERVEROUTPUT ON;

-- ===== 1. FUNCTIONS EM CONSULTAS SQL =====

-- 1.1 Painel de pacientes: indicador + resumo formatado direto no SELECT
SELECT u.ID_USUARIO,
       u.NM_COMPLETO,
       FN_SHAS_TAXA_CONTROLE(u.ID_USUARIO, 30)   AS PC_NA_META_30D,
       FN_SHAS_RESUMO_PACIENTE(u.ID_USUARIO, 30) AS RESUMO
  FROM T_SHAS_USUARIO u
 WHERE u.TP_PERFIL = 'USER'
 ORDER BY PC_NA_META_30D NULLS LAST;

-- 1.2 Function no WHERE: pacientes com controle abaixo de 60%
SELECT NM_COMPLETO, FN_SHAS_TAXA_CONTROLE(ID_USUARIO, 30) AS PC_NA_META
  FROM T_SHAS_USUARIO
 WHERE TP_PERFIL = 'USER'
   AND FN_SHAS_TAXA_CONTROLE(ID_USUARIO, 30) < 60;

-- 1.3 Function no GROUP BY: distribuicao das leituras por classificacao
SELECT FN_SHAS_CLASSIFICAR(VL_SISTOLICA, VL_DIASTOLICA, 'ROTULO') AS CLASSIFICACAO,
       TP_ORIGEM,
       COUNT(*) AS QTD
  FROM T_SHAS_MEDICAO
 GROUP BY FN_SHAS_CLASSIFICAR(VL_SISTOLICA, VL_DIASTOLICA, 'ROTULO'), TP_ORIGEM
 ORDER BY 1, 2;

-- 1.4 Historico legivel das ultimas 10 leituras (dados formatados)
SELECT * FROM (
  SELECT u.NM_COMPLETO, FN_SHAS_FORMATAR_MEDICAO(m.ID_MEDICAO) AS LEITURA
    FROM T_SHAS_MEDICAO m JOIN T_SHAS_USUARIO u ON u.ID_USUARIO = m.ID_USUARIO
   ORDER BY m.DT_MEDICAO DESC
) WHERE ROWNUM <= 10;

-- ===== 2. PROCEDURES =====

-- 2.1 Registrar alerta para uma leitura critica nova
DECLARE
  v_id_medicao NUMBER;
  v_id_alerta  NUMBER;
  v_sev        VARCHAR2(10);
BEGIN
  INSERT INTO T_SHAS_MEDICAO (ID_USUARIO, VL_SISTOLICA, VL_DIASTOLICA, DT_MEDICAO, DS_OBSERVACAO)
  SELECT ID_USUARIO, 186, 121, CAST(SYSTIMESTAMP AS TIMESTAMP), 'Teste de crise hipertensiva'
    FROM T_SHAS_USUARIO WHERE DS_EMAIL = 'paciente@smarthas.com'
  RETURNING ID_MEDICAO INTO v_id_medicao;

  PRC_SHAS_REGISTRAR_ALERTA(v_id_medicao, v_id_alerta, v_sev);
  DBMS_OUTPUT.PUT_LINE('Medicao ' || v_id_medicao || ' -> alerta ' || NVL(TO_CHAR(v_id_alerta), '-')
                       || ' (' || v_sev || ')');

  -- Reprocessar a mesma leitura NAO duplica o alerta (idempotencia)
  PRC_SHAS_REGISTRAR_ALERTA(v_id_medicao, v_id_alerta, v_sev);
  DBMS_OUTPUT.PUT_LINE('Reprocessamento -> mesmo alerta ' || v_id_alerta);
  COMMIT;
END;
/

-- 2.2 Relatorio resumido de todos os pacientes (30 dias)
DECLARE
  v_qtd NUMBER;
BEGIN
  PRC_SHAS_GERAR_RELATORIO_RESUMO(p_dias => 30, p_id_usuario => NULL, p_qt_gerados => v_qtd);
  DBMS_OUTPUT.PUT_LINE('Relatorios gerados: ' || v_qtd);
END;
/

SELECT r.ID_RELATORIO, u.NM_COMPLETO, r.QT_MEDICOES, r.VL_MEDIA_SISTOLICA, r.VL_MEDIA_DIASTOLICA,
       r.PC_CONTROLE, r.QT_ALERTAS_ABERTOS, r.NV_RISCO
  FROM T_SHAS_RELATORIO_RESUMO r JOIN T_SHAS_USUARIO u ON u.ID_USUARIO = r.ID_USUARIO
 ORDER BY r.ID_RELATORIO DESC
 FETCH FIRST 10 ROWS ONLY;

-- 2.3 Alertas abertos por severidade
SELECT a.NV_SEVERIDADE, a.TP_ALERTA, u.NM_COMPLETO, a.DS_MENSAGEM,
       TO_CHAR(a.DT_ALERTA, 'DD/MM HH24:MI') AS QUANDO
  FROM T_SHAS_ALERTA a JOIN T_SHAS_USUARIO u ON u.ID_USUARIO = a.ID_USUARIO
 WHERE a.ST_ALERTA = 'ABERTO'
 ORDER BY DECODE(a.NV_SEVERIDADE, 'CRITICO', 1, 'ALTO', 2, 3), a.DT_ALERTA DESC;

-- ===== 3. TRATAMENTO DE EXCECOES (cenarios de erro esperados) =====
BEGIN
  DBMS_OUTPUT.PUT_LINE(FN_SHAS_TAXA_CONTROLE(999999, 30));
EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Esperado -> ' || SQLERRM);
END;
/
DECLARE v_id NUMBER; v_sev VARCHAR2(10);
BEGIN
  PRC_SHAS_REGISTRAR_ALERTA(999999, v_id, v_sev);
EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Esperado -> ' || SQLERRM);
END;
/
DECLARE v_qtd NUMBER; v_disp NUMBER;
BEGIN
  SELECT ID_DISPOSITIVO INTO v_disp FROM T_SHAS_DISPOSITIVO WHERE CD_SERIE = 'SHAS-BP-0004';
  PRC_SHAS_SIMULAR_LEITURAS_SENSOR(v_disp, 5, 30, v_qtd);
EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Esperado (dispositivo inativo) -> ' || SQLERRM);
END;
/

-- 4. Auditoria: log das rotinas
SELECT NM_ROTINA, TP_EVENTO, DS_MENSAGEM, TO_CHAR(DT_LOG, 'DD/MM HH24:MI:SS') AS QUANDO
  FROM T_SHAS_LOG_PROCESSAMENTO ORDER BY ID_LOG DESC;
