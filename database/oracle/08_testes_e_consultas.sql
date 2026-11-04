-- ============================================================
-- Smart HAS - Fase 7 | 08_testes_e_consultas.sql
-- Demonstra os packages, o SQL dinamico e as triggers, incluindo
-- os cenarios de erro. Roda no SQL Developer (F5) e no SQL*Plus
-- (usado pelo pipeline CI/CD).
-- ============================================================
SET SERVEROUTPUT ON;
SET DEFINE OFF;
SET LINESIZE 200;
SET PAGESIZE 100;

-- ===== 0. OBJETOS DO PROJETO (todos devem estar VALID) =====
SELECT object_type, object_name, status
  FROM user_objects
 WHERE object_name LIKE 'PKG\_SHAS\_%' ESCAPE '\'
    OR object_name LIKE 'TRG\_SHAS\_%' ESCAPE '\'
 ORDER BY object_type, object_name;

-- ===== 1. FUNCTIONS DOS PACKAGES EM CONSULTAS SQL =====

-- 1.1 Painel de pacientes: indicador + resumo formatado no SELECT
SELECT u.ID_USUARIO,
       u.NM_COMPLETO,
       PKG_SHAS_INDICADORES.FN_TAXA_CONTROLE(u.ID_USUARIO, 30)   AS PC_NA_META_30D,
       PKG_SHAS_INDICADORES.FN_RESUMO_PACIENTE(u.ID_USUARIO, 30) AS RESUMO
  FROM T_SHAS_USUARIO u
 WHERE u.TP_PERFIL = 'USER'
 ORDER BY PC_NA_META_30D NULLS LAST;

-- 1.2 Function no GROUP BY: distribuicao por classificacao e origem
SELECT PKG_SHAS_UTIL.FN_CLASSIFICAR(VL_SISTOLICA, VL_DIASTOLICA, 'ROTULO') AS CLASSIFICACAO,
       TP_ORIGEM,
       COUNT(*) AS QTD
  FROM T_SHAS_MEDICAO
 GROUP BY PKG_SHAS_UTIL.FN_CLASSIFICAR(VL_SISTOLICA, VL_DIASTOLICA, 'ROTULO'), TP_ORIGEM
 ORDER BY 1, 2;

-- 1.3 Historico legivel das ultimas 5 leituras
SELECT * FROM (
  SELECT u.NM_COMPLETO, PKG_SHAS_UTIL.FN_FORMATAR_MEDICAO(m.ID_MEDICAO) AS LEITURA
    FROM T_SHAS_MEDICAO m JOIN T_SHAS_USUARIO u ON u.ID_USUARIO = m.ID_USUARIO
   ORDER BY m.DT_MEDICAO DESC
) WHERE ROWNUM <= 5;

-- ===== 2. PROCEDURES DOS PACKAGES =====

-- 2.1 Alerta para uma leitura critica nova (+ idempotencia)
DECLARE
  v_id_medicao NUMBER;
  v_id_alerta  NUMBER;
  v_sev        VARCHAR2(10);
BEGIN
  INSERT INTO T_SHAS_MEDICAO (ID_USUARIO, VL_SISTOLICA, VL_DIASTOLICA, DT_MEDICAO, DS_OBSERVACAO)
  SELECT ID_USUARIO, 186, 121, CAST(SYSTIMESTAMP AS TIMESTAMP), 'Teste de crise hipertensiva'
    FROM T_SHAS_USUARIO WHERE DS_EMAIL = 'paciente@smarthas.com'
  RETURNING ID_MEDICAO INTO v_id_medicao;

  PKG_SHAS_ALERTAS.PRC_REGISTRAR_ALERTA(v_id_medicao, v_id_alerta, v_sev);
  DBMS_OUTPUT.PUT_LINE('Medicao ' || v_id_medicao || ' -> alerta ' || NVL(TO_CHAR(v_id_alerta), '-')
                       || ' (' || v_sev || ')');

  PKG_SHAS_ALERTAS.PRC_REGISTRAR_ALERTA(v_id_medicao, v_id_alerta, v_sev);
  DBMS_OUTPUT.PUT_LINE('Reprocessamento -> mesmo alerta ' || v_id_alerta);
  COMMIT;
END;
/

-- 2.2 Relatorio resumido de todos os pacientes (30 dias)
DECLARE
  v_qtd NUMBER;
BEGIN
  PKG_SHAS_RELATORIOS.PRC_GERAR_RELATORIO_RESUMO(p_dias => 30, p_id_usuario => NULL, p_qt_gerados => v_qtd);
  DBMS_OUTPUT.PUT_LINE('Relatorios gerados: ' || v_qtd);
END;
/

SELECT r.ID_RELATORIO, u.NM_COMPLETO, r.QT_MEDICOES, r.PC_CONTROLE, r.QT_ALERTAS_ABERTOS, r.NV_RISCO
  FROM T_SHAS_RELATORIO_RESUMO r JOIN T_SHAS_USUARIO u ON u.ID_USUARIO = r.ID_USUARIO
 ORDER BY r.ID_RELATORIO DESC
 FETCH FIRST 6 ROWS ONLY;

-- ===== 3. DYNAMIC SQL =====

-- 3.1 Busca combinando filtros em tempo real: hipertensao, ultimos 30 dias,
--     ordenada pela maior sistolica. O SQL gerado e devolvido em p_sql.
VARIABLE rc_busca REFCURSOR
VARIABLE v_total NUMBER
VARIABLE v_sql VARCHAR2(2000)
BEGIN
  PKG_SHAS_INDICADORES.PRC_CONSULTAR_MEDICOES(
    p_classificacao => 'HYPERTENSION',
    p_dias          => 30,
    p_ordenar_por   => 'SISTOLICA',
    p_direcao       => 'DESC',
    p_limite        => 5,
    p_total         => :v_total,
    p_sql           => :v_sql,
    p_cursor        => :rc_busca);
END;
/
PRINT v_total
PRINT v_sql
PRINT rc_busca

-- 3.2 Mesma procedure, outra combinacao: so leituras de SENSOR de um paciente
DECLARE
  v_cur    SYS_REFCURSOR;
  v_total  NUMBER;
  v_sql    VARCHAR2(2000);
  v_id     NUMBER; v_usu NUMBER; v_nome VARCHAR2(120);
  v_sis    NUMBER; v_dia NUMBER; v_fc NUMBER; v_dt TIMESTAMP; v_origem VARCHAR2(10); v_classif VARCHAR2(20);
  v_paciente NUMBER;
BEGIN
  SELECT ID_USUARIO INTO v_paciente FROM T_SHAS_USUARIO WHERE DS_EMAIL = 'paciente@smarthas.com';
  PKG_SHAS_INDICADORES.PRC_CONSULTAR_MEDICOES(
    p_id_usuario => v_paciente, p_origem => 'SENSOR', p_ordenar_por => 'DATA', p_limite => 3,
    p_total => v_total, p_sql => v_sql, p_cursor => v_cur);
  DBMS_OUTPUT.PUT_LINE('Leituras de sensor do paciente: ' || v_total || ' (mostrando 3)');
  LOOP
    FETCH v_cur INTO v_id, v_usu, v_nome, v_sis, v_dia, v_fc, v_dt, v_origem, v_classif;
    EXIT WHEN v_cur%NOTFOUND;
    DBMS_OUTPUT.PUT_LINE('  ' || TO_CHAR(v_dt, 'DD/MM HH24:MI') || ' ' || v_sis || 'x' || v_dia
                         || ' ' || v_origem || ' ' || v_classif);
  END LOOP;
  CLOSE v_cur;
END;
/

-- 3.3 GROUP BY dinamico: a dimensao e escolhida em tempo de execucao
VARIABLE rc_dim REFCURSOR
EXEC :rc_dim := PKG_SHAS_INDICADORES.FN_ESTATISTICA_POR_DIMENSAO('CLASSIFICACAO', 30);
PRINT rc_dim
EXEC :rc_dim := PKG_SHAS_INDICADORES.FN_ESTATISTICA_POR_DIMENSAO('PERIODO_DIA', 30);
PRINT rc_dim

-- ===== 4. TRIGGERS =====

-- 4.1 TRG_SHAS_ALERTA_STATUS: basta mudar o status; DT_RESOLUCAO e preenchida
--     pelo banco. TRG_SHAS_ALERTA_AUDITORIA grava a alteracao.
DECLARE
  v_qtd NUMBER;
  v_usu NUMBER;
BEGIN
  SELECT ID_USUARIO INTO v_usu FROM T_SHAS_USUARIO WHERE DS_EMAIL = 'maria.souza@smarthas.com';
  PKG_SHAS_ALERTAS.PRC_RESOLVER_ALERTAS_PACIENTE(v_usu, v_qtd);
  DBMS_OUTPUT.PUT_LINE('Alertas resolvidos (Maria): ' || v_qtd);

  UPDATE T_SHAS_ALERTA SET ST_ALERTA = 'RESOLVIDO'
   WHERE ID_ALERTA = (SELECT MIN(ID_ALERTA) FROM T_SHAS_ALERTA WHERE ST_ALERTA = 'ABERTO');
  DBMS_OUTPUT.PUT_LINE('Alerta resolvido via UPDATE direto: ' || SQL%ROWCOUNT);
  COMMIT;
END;
/

SELECT ID_ALERTA, ST_ALERTA, TO_CHAR(DT_RESOLUCAO, 'DD/MM HH24:MI:SS') AS RESOLVIDO_EM
  FROM T_SHAS_ALERTA WHERE ST_ALERTA = 'RESOLVIDO' ORDER BY ID_ALERTA;

-- Historico de auditoria (as ultimas operacoes)
SELECT ID_AUDITORIA, ID_ALERTA, TP_OPERACAO, ST_ANTERIOR, ST_NOVO, NV_SEVERIDADE, DS_ORIGEM,
       TO_CHAR(DT_OPERACAO, 'DD/MM HH24:MI:SS') AS QUANDO
  FROM T_SHAS_AUDITORIA_ALERTA ORDER BY ID_AUDITORIA DESC FETCH FIRST 10 ROWS ONLY;

-- 4.2 TRG_SHAS_MEDICAO_DISPOSITIVO: leitura via aparelho ativo -> origem SENSOR
--     e DT_ULTIMA_SYNC atualizada automaticamente
DECLARE
  v_disp   NUMBER; v_usu NUMBER; v_med NUMBER; v_origem VARCHAR2(10); v_sync TIMESTAMP;
BEGIN
  SELECT ID_DISPOSITIVO, ID_USUARIO INTO v_disp, v_usu FROM T_SHAS_DISPOSITIVO WHERE CD_SERIE = 'SHAS-BP-0002';
  INSERT INTO T_SHAS_MEDICAO (ID_USUARIO, ID_DISPOSITIVO, VL_SISTOLICA, VL_DIASTOLICA, DT_MEDICAO)
  VALUES (v_usu, v_disp, 118, 76, CAST(SYSTIMESTAMP AS TIMESTAMP))
  RETURNING ID_MEDICAO, TP_ORIGEM INTO v_med, v_origem;
  SELECT DT_ULTIMA_SYNC INTO v_sync FROM T_SHAS_DISPOSITIVO WHERE ID_DISPOSITIVO = v_disp;
  DBMS_OUTPUT.PUT_LINE('Medicao ' || v_med || ' origem=' || v_origem
                       || ' | ultima sync do aparelho: ' || TO_CHAR(v_sync, 'DD/MM HH24:MI:SS'));
  COMMIT;
END;
/

-- ===== 5. TRATAMENTO DE EXCECOES (cenarios de erro esperados) =====
BEGIN
  DBMS_OUTPUT.PUT_LINE(PKG_SHAS_INDICADORES.FN_TAXA_CONTROLE(999999, 30));
EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Esperado -> ' || SQLERRM);
END;
/
DECLARE v_id NUMBER; v_sev VARCHAR2(10);
BEGIN
  PKG_SHAS_ALERTAS.PRC_REGISTRAR_ALERTA(999999, v_id, v_sev);
EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Esperado -> ' || SQLERRM);
END;
/
-- Trigger bloqueia leitura de aparelho inativo
DECLARE v_disp NUMBER; v_usu NUMBER;
BEGIN
  SELECT ID_DISPOSITIVO, ID_USUARIO INTO v_disp, v_usu FROM T_SHAS_DISPOSITIVO WHERE CD_SERIE = 'SHAS-BP-0004';
  INSERT INTO T_SHAS_MEDICAO (ID_USUARIO, ID_DISPOSITIVO, VL_SISTOLICA, VL_DIASTOLICA, DT_MEDICAO)
  VALUES (v_usu, v_disp, 130, 85, CAST(SYSTIMESTAMP AS TIMESTAMP));
EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Esperado (trigger, aparelho inativo) -> ' || SQLERRM);
END;
/
-- Trigger bloqueia aparelho de outro paciente
DECLARE v_disp NUMBER; v_outro NUMBER;
BEGIN
  SELECT ID_DISPOSITIVO INTO v_disp FROM T_SHAS_DISPOSITIVO WHERE CD_SERIE = 'SHAS-BP-0001';
  SELECT ID_USUARIO INTO v_outro FROM T_SHAS_USUARIO WHERE DS_EMAIL = 'roberto.ferreira@smarthas.com';
  INSERT INTO T_SHAS_MEDICAO (ID_USUARIO, ID_DISPOSITIVO, VL_SISTOLICA, VL_DIASTOLICA, DT_MEDICAO)
  VALUES (v_outro, v_disp, 130, 85, CAST(SYSTIMESTAMP AS TIMESTAMP));
EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Esperado (trigger, aparelho de outro paciente) -> ' || SQLERRM);
END;
/
-- SQL dinamico recusa ordenacao fora da whitelist (protecao contra SQL injection)
DECLARE v_cur SYS_REFCURSOR; v_total NUMBER; v_sql VARCHAR2(2000);
BEGIN
  PKG_SHAS_INDICADORES.PRC_CONSULTAR_MEDICOES(
    p_ordenar_por => 'DT_MEDICAO; DROP TABLE T_SHAS_MEDICAO', p_total => v_total, p_sql => v_sql, p_cursor => v_cur);
EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Esperado (whitelist) -> ' || SQLERRM);
END;
/

-- ===== 6. LOG DAS ROTINAS =====
SELECT NM_ROTINA, TP_EVENTO, SUBSTR(DS_MENSAGEM, 1, 100) AS MENSAGEM, TO_CHAR(DT_LOG, 'DD/MM HH24:MI:SS') AS QUANDO
  FROM T_SHAS_LOG_PROCESSAMENTO ORDER BY ID_LOG DESC FETCH FIRST 15 ROWS ONLY;
