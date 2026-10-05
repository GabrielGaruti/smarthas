-- ============================================================
-- Smart HAS - Fase 6 | 03_procedures.sql
-- Procedures com logica de negocio executada no banco.
--
--  PRC_SHAS_REGISTRAR_ALERTA ......... avalia UMA leitura e gera alerta
--      >>> acionada pelo back-end Java (evento "medicao registrada")
--  PRC_SHAS_GERAR_RELATORIO_RESUMO ... relatorio consolidado por paciente
--      >>> acionada pelo painel admin (REST -> Java -> JDBC -> Oracle)
--  PRC_SHAS_SIMULAR_LEITURAS_SENSOR .. importa leituras simuladas de IoT
--
-- Codigos de erro: -20010 medicao inexistente | -20011 periodo invalido
--                  -20012 dispositivo inexistente/inativo | -20013 qtd invalida
-- ============================================================

-- ------------------------------------------------------------
-- PRC_SHAS_REGISTRAR_ALERTA
-- Objetivo: analisar uma leitura recem-gravada e registrar um
-- alerta clinico quando ela for critica.
-- Regras (IF/ELSIF):
--   1) sis >= 180 ou dia >= 120 ......... CRITICO  / CRISE_HIPERTENSIVA
--   2) sis >= 140 ou dia >= 90 e, entre as 5 ultimas leituras,
--      3 ou mais sao hipertensao (CURSOR + LOOP) ... ALTO / TENDENCIA_ALTA
--   3) sis >= 140 ou dia >= 90 ............ MODERADO / HIPERTENSAO
--   4) demais ............................. nenhum alerta
-- Saidas: p_id_alerta (NULL se nao gerou) e p_severidade.
-- Idempotente: reprocessar a mesma leitura devolve o alerta ja existente
-- (constraint UK_SHAS_ALERTA_MED_TIPO + DUP_VAL_ON_INDEX).
-- Nao executa COMMIT: quem chama controla a transacao.
-- ------------------------------------------------------------
CREATE OR REPLACE PROCEDURE PRC_SHAS_REGISTRAR_ALERTA (
  p_id_medicao  IN  NUMBER,
  p_id_alerta   OUT NUMBER,
  p_severidade  OUT VARCHAR2
) AS
  c_rotina   CONSTANT VARCHAR2(30) := 'PRC_SHAS_REGISTRAR_ALERTA';

  v_id_usuario  T_SHAS_MEDICAO.ID_USUARIO%TYPE;
  v_sis         T_SHAS_MEDICAO.VL_SISTOLICA%TYPE;
  v_dia         T_SHAS_MEDICAO.VL_DIASTOLICA%TYPE;
  v_dt_medicao  T_SHAS_MEDICAO.DT_MEDICAO%TYPE;
  v_tipo        T_SHAS_ALERTA.TP_ALERTA%TYPE;
  v_mensagem    T_SHAS_ALERTA.DS_MENSAGEM%TYPE;
  v_qtd_hiper   PLS_INTEGER := 0;

  -- Cursor explicito: 5 leituras mais recentes do paciente ate a leitura atual
  CURSOR c_recentes (pc_usuario NUMBER, pc_ate TIMESTAMP) IS
    SELECT VL_SISTOLICA, VL_DIASTOLICA
      FROM (SELECT VL_SISTOLICA, VL_DIASTOLICA
              FROM T_SHAS_MEDICAO
             WHERE ID_USUARIO = pc_usuario
               AND DT_MEDICAO <= pc_ate
             ORDER BY DT_MEDICAO DESC)
     WHERE ROWNUM <= 5;
  r_rec c_recentes%ROWTYPE;
BEGIN
  p_id_alerta  := NULL;
  p_severidade := 'NENHUM';

  SELECT ID_USUARIO, VL_SISTOLICA, VL_DIASTOLICA, DT_MEDICAO
    INTO v_id_usuario, v_sis, v_dia, v_dt_medicao
    FROM T_SHAS_MEDICAO
   WHERE ID_MEDICAO = p_id_medicao;

  IF v_sis >= 180 OR v_dia >= 120 THEN
    v_tipo       := 'CRISE_HIPERTENSIVA';
    p_severidade := 'CRITICO';
    v_mensagem   := 'Leitura de ' || v_sis || 'x' || v_dia
                 || ' mmHg compativel com crise hipertensiva. Procure atendimento imediato.';

  ELSIF v_sis >= 140 OR v_dia >= 90 THEN
    -- Conta quantas das ultimas 5 leituras sao hipertensao
    OPEN c_recentes(v_id_usuario, v_dt_medicao);
    LOOP
      FETCH c_recentes INTO r_rec;
      EXIT WHEN c_recentes%NOTFOUND;
      IF FN_SHAS_CLASSIFICAR(r_rec.VL_SISTOLICA, r_rec.VL_DIASTOLICA) = 'HYPERTENSION' THEN
        v_qtd_hiper := v_qtd_hiper + 1;
      END IF;
    END LOOP;
    CLOSE c_recentes;

    IF v_qtd_hiper >= 3 THEN
      v_tipo       := 'TENDENCIA_ALTA';
      p_severidade := 'ALTO';
      v_mensagem   := v_qtd_hiper || ' das ultimas 5 leituras indicam hipertensao (ultima: '
                   || v_sis || 'x' || v_dia || ' mmHg). Agende avaliacao medica.';
    ELSE
      v_tipo       := 'HIPERTENSAO';
      p_severidade := 'MODERADO';
      v_mensagem   := 'Leitura de ' || v_sis || 'x' || v_dia
                   || ' mmHg acima da meta (140x90). Repita a medicao em repouso.';
    END IF;

  ELSE
    RETURN;  -- leitura dentro do esperado: nenhum alerta
  END IF;

  BEGIN
    INSERT INTO T_SHAS_ALERTA (ID_USUARIO, ID_MEDICAO, TP_ALERTA, NV_SEVERIDADE, DS_MENSAGEM)
    VALUES (v_id_usuario, p_id_medicao, v_tipo, p_severidade, v_mensagem)
    RETURNING ID_ALERTA INTO p_id_alerta;
  EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
      SELECT ID_ALERTA INTO p_id_alerta
        FROM T_SHAS_ALERTA
       WHERE ID_MEDICAO = p_id_medicao AND TP_ALERTA = v_tipo;
  END;

EXCEPTION
  WHEN NO_DATA_FOUND THEN
    PRC_SHAS_LOG(c_rotina, 'AVISO', 'Medicao ' || p_id_medicao || ' nao encontrada.');
    RAISE_APPLICATION_ERROR(-20010, 'Medicao ' || p_id_medicao || ' nao encontrada.');
  WHEN OTHERS THEN
    IF c_recentes%ISOPEN THEN
      CLOSE c_recentes;
    END IF;
    PRC_SHAS_LOG(c_rotina, 'ERRO', 'Medicao ' || p_id_medicao || ': ' || SQLERRM);
    RAISE;
END PRC_SHAS_REGISTRAR_ALERTA;
/

-- ------------------------------------------------------------
-- PRC_SHAS_GERAR_RELATORIO_RESUMO
-- Objetivo: rotina batch que consolida, para cada paciente (ou um
-- paciente especifico), os indicadores dos ultimos p_dias dias e grava
-- em T_SHAS_RELATORIO_RESUMO. Usa CURSOR FOR LOOP, as functions
-- FN_SHAS_TAXA_CONTROLE / FN_SHAS_RESUMO_PACIENTE e tratamento de
-- erro POR PACIENTE (uma falha nao interrompe os demais).
-- Classificacao de risco:
--   SEM DADOS  : nenhuma leitura no periodo
--   ALTO       : alerta CRITICO aberto ou taxa de controle < 50%
--   MODERADO   : taxa de controle < 80%
--   BAIXO      : demais casos
-- Saida: p_qt_gerados = quantidade de relatorios gravados.
-- ------------------------------------------------------------
CREATE OR REPLACE PROCEDURE PRC_SHAS_GERAR_RELATORIO_RESUMO (
  p_dias        IN  NUMBER DEFAULT 30,
  p_id_usuario  IN  NUMBER DEFAULT NULL,   -- NULL = todos os pacientes
  p_qt_gerados  OUT NUMBER
) AS
  c_rotina   CONSTANT VARCHAR2(40) := 'PRC_SHAS_GERAR_RELATORIO_RESUMO';

  CURSOR c_pacientes IS
    SELECT ID_USUARIO, NM_COMPLETO
      FROM T_SHAS_USUARIO
     WHERE TP_PERFIL = 'USER'
       AND (p_id_usuario IS NULL OR ID_USUARIO = p_id_usuario)
     ORDER BY ID_USUARIO;

  v_inicio      DATE := TRUNC(SYSDATE) - p_dias;
  v_qtd         NUMBER;
  v_media_sis   NUMBER;
  v_media_dia   NUMBER;
  v_max_sis     NUMBER;
  v_taxa        NUMBER;
  v_alertas     NUMBER;
  v_criticos    NUMBER;
  v_risco       VARCHAR2(10);
  v_falhas      PLS_INTEGER := 0;
BEGIN
  p_qt_gerados := 0;

  IF p_dias IS NULL OR p_dias <= 0 OR p_dias > 365 THEN
    RAISE_APPLICATION_ERROR(-20011, 'Periodo invalido: informe entre 1 e 365 dias.');
  END IF;

  FOR r IN c_pacientes LOOP
    BEGIN
      SELECT COUNT(*), ROUND(AVG(VL_SISTOLICA), 1), ROUND(AVG(VL_DIASTOLICA), 1), MAX(VL_SISTOLICA)
        INTO v_qtd, v_media_sis, v_media_dia, v_max_sis
        FROM T_SHAS_MEDICAO
       WHERE ID_USUARIO = r.ID_USUARIO
         AND DT_MEDICAO >= SYSTIMESTAMP - NUMTODSINTERVAL(p_dias, 'DAY');

      SELECT COUNT(*), SUM(CASE WHEN NV_SEVERIDADE = 'CRITICO' THEN 1 ELSE 0 END)
        INTO v_alertas, v_criticos
        FROM T_SHAS_ALERTA
       WHERE ID_USUARIO = r.ID_USUARIO AND ST_ALERTA = 'ABERTO';

      v_taxa := FN_SHAS_TAXA_CONTROLE(r.ID_USUARIO, p_dias);

      IF v_qtd = 0 THEN
        v_risco := 'SEM DADOS';
      ELSIF NVL(v_criticos, 0) > 0 OR v_taxa < 50 THEN
        v_risco := 'ALTO';
      ELSIF v_taxa < 80 THEN
        v_risco := 'MODERADO';
      ELSE
        v_risco := 'BAIXO';
      END IF;

      INSERT INTO T_SHAS_RELATORIO_RESUMO (
        ID_USUARIO, DT_INICIO, DT_FIM, QT_MEDICOES, VL_MEDIA_SISTOLICA, VL_MEDIA_DIASTOLICA,
        VL_MAX_SISTOLICA, PC_CONTROLE, QT_ALERTAS_ABERTOS, NV_RISCO, DS_RESUMO)
      VALUES (
        r.ID_USUARIO, v_inicio, TRUNC(SYSDATE), v_qtd, v_media_sis, v_media_dia,
        v_max_sis, v_taxa, v_alertas, v_risco,
        SUBSTR(FN_SHAS_RESUMO_PACIENTE(r.ID_USUARIO, p_dias), 1, 400));

      p_qt_gerados := p_qt_gerados + 1;
      DBMS_OUTPUT.PUT_LINE(RPAD(r.NM_COMPLETO, 30) || ' -> risco ' || v_risco
                           || ' | taxa ' || NVL(TO_CHAR(v_taxa), '-') || '%');
    EXCEPTION
      WHEN OTHERS THEN
        v_falhas := v_falhas + 1;
        PRC_SHAS_LOG(c_rotina, 'ERRO', 'Paciente ' || r.ID_USUARIO || ': ' || SQLERRM);
    END;
  END LOOP;

  COMMIT;
  PRC_SHAS_LOG(c_rotina, CASE WHEN v_falhas = 0 THEN 'INFO' ELSE 'AVISO' END,
               p_qt_gerados || ' relatorio(s) gerado(s), ' || v_falhas
               || ' falha(s), periodo de ' || p_dias || ' dias.');
EXCEPTION
  WHEN OTHERS THEN
    ROLLBACK;
    PRC_SHAS_LOG(c_rotina, 'ERRO', SQLERRM);
    RAISE;
END PRC_SHAS_GERAR_RELATORIO_RESUMO;
/

-- ------------------------------------------------------------
-- PRC_SHAS_SIMULAR_LEITURAS_SENSOR
-- Objetivo: simular a importacao de leituras de um monitor IoT,
-- distribuidas nos ultimos p_dias dias (DBMS_RANDOM). Cada leitura
-- inserida passa por PRC_SHAS_REGISTRAR_ALERTA (reuso de regra).
-- Saida: p_qt_inseridas.
-- ------------------------------------------------------------
CREATE OR REPLACE PROCEDURE PRC_SHAS_SIMULAR_LEITURAS_SENSOR (
  p_id_dispositivo IN  NUMBER,
  p_quantidade     IN  NUMBER,
  p_dias           IN  NUMBER DEFAULT 30,
  p_qt_inseridas   OUT NUMBER
) AS
  c_rotina   CONSTANT VARCHAR2(40) := 'PRC_SHAS_SIMULAR_LEITURAS_SENSOR';
  v_id_usuario  T_SHAS_DISPOSITIVO.ID_USUARIO%TYPE;
  v_ativo       T_SHAS_DISPOSITIVO.ST_ATIVO%TYPE;
  v_sis         NUMBER;
  v_dia         NUMBER;
  v_fc          NUMBER;
  v_id_medicao  NUMBER;
  v_id_alerta   NUMBER;
  v_sev         VARCHAR2(10);
BEGIN
  p_qt_inseridas := 0;

  IF p_quantidade IS NULL OR p_quantidade NOT BETWEEN 1 AND 500 THEN
    RAISE_APPLICATION_ERROR(-20013, 'Quantidade deve estar entre 1 e 500.');
  END IF;

  SELECT ID_USUARIO, ST_ATIVO INTO v_id_usuario, v_ativo
    FROM T_SHAS_DISPOSITIVO WHERE ID_DISPOSITIVO = p_id_dispositivo;

  IF v_ativo = 0 THEN
    RAISE_APPLICATION_ERROR(-20012, 'Dispositivo ' || p_id_dispositivo || ' esta inativo.');
  END IF;

  FOR i IN 1 .. p_quantidade LOOP
    v_sis := ROUND(DBMS_RANDOM.VALUE(105, 172));
    v_dia := LEAST(ROUND(DBMS_RANDOM.VALUE(65, 108)), v_sis - 25);  -- mantem coerencia fisiologica
    v_fc  := ROUND(DBMS_RANDOM.VALUE(58, 104));

    INSERT INTO T_SHAS_MEDICAO (ID_USUARIO, ID_DISPOSITIVO, VL_SISTOLICA, VL_DIASTOLICA,
                                VL_FREQ_CARDIACA, DT_MEDICAO, TP_ORIGEM, DS_OBSERVACAO)
    VALUES (v_id_usuario, p_id_dispositivo, v_sis, v_dia, v_fc,
            CAST(SYSTIMESTAMP AS TIMESTAMP) - NUMTODSINTERVAL(DBMS_RANDOM.VALUE(0, NVL(p_dias, 30)), 'DAY'),
            'SENSOR', 'Leitura automatica do dispositivo ' || p_id_dispositivo)
    RETURNING ID_MEDICAO INTO v_id_medicao;

    PRC_SHAS_REGISTRAR_ALERTA(v_id_medicao, v_id_alerta, v_sev);
    p_qt_inseridas := p_qt_inseridas + 1;
  END LOOP;

  UPDATE T_SHAS_DISPOSITIVO SET DT_ULTIMA_SYNC = SYSTIMESTAMP
   WHERE ID_DISPOSITIVO = p_id_dispositivo;

  COMMIT;
  PRC_SHAS_LOG(c_rotina, 'INFO', p_qt_inseridas || ' leitura(s) importada(s) do dispositivo ' || p_id_dispositivo);
EXCEPTION
  WHEN NO_DATA_FOUND THEN
    ROLLBACK;
    RAISE_APPLICATION_ERROR(-20012, 'Dispositivo ' || p_id_dispositivo || ' nao encontrado.');
  WHEN OTHERS THEN
    ROLLBACK;
    PRC_SHAS_LOG(c_rotina, 'ERRO', 'Dispositivo ' || p_id_dispositivo || ': ' || SQLERRM);
    RAISE;
END PRC_SHAS_SIMULAR_LEITURAS_SENSOR;
/
