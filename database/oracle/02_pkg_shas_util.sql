-- ============================================================
-- Smart HAS - Fase 7 | 02_pkg_shas_util.sql
-- PKG_SHAS_UTIL: rotinas de apoio usadas por todos os outros
-- packages e pelas triggers.
--
--  PRC_LOG ............ grava log em transacao autonoma
--  FN_CLASSIFICAR ..... classifica uma leitura (mesma regra do Java)
--  FN_FORMATAR_MEDICAO  linha legivel de uma leitura
--
-- Fase 6 -> Fase 7: PRC_SHAS_LOG, FN_SHAS_CLASSIFICAR e
-- FN_SHAS_FORMATAR_MEDICAO viraram membros deste package.
--
-- Codigos de erro (RAISE_APPLICATION_ERROR):
--  -20003 parametros nulos | -20004 medicao inexistente
-- ============================================================

CREATE OR REPLACE PACKAGE PKG_SHAS_UTIL AS

  -- Tipos de evento aceitos por T_SHAS_LOG_PROCESSAMENTO
  c_info  CONSTANT VARCHAR2(10) := 'INFO';
  c_aviso CONSTANT VARCHAR2(10) := 'AVISO';
  c_erro  CONSTANT VARCHAR2(10) := 'ERRO';

  -- Meta terapeutica (sistolica < 140 E diastolica < 90)
  c_meta_sistolica  CONSTANT PLS_INTEGER := 140;
  c_meta_diastolica CONSTANT PLS_INTEGER := 90;

  -- Grava um evento em T_SHAS_LOG_PROCESSAMENTO (PRAGMA AUTONOMOUS_TRANSACTION).
  PROCEDURE PRC_LOG (
    p_rotina   IN VARCHAR2,
    p_evento   IN VARCHAR2,
    p_mensagem IN VARCHAR2
  );

  -- NORMAL / ELEVATED / HYPERTENSION (p_formato = 'CODIGO') ou
  -- Normal / Elevada / Hipertensao (p_formato = 'ROTULO').
  FUNCTION FN_CLASSIFICAR (
    p_sistolica  IN NUMBER,
    p_diastolica IN NUMBER,
    p_formato    IN VARCHAR2 DEFAULT 'CODIGO'
  ) RETURN VARCHAR2 DETERMINISTIC;

  -- Ex.: 03/10/2026 07:15 | 145x95 mmHg | FC 82 bpm | Hipertensao | SENSOR
  FUNCTION FN_FORMATAR_MEDICAO (
    p_id_medicao IN NUMBER
  ) RETURN VARCHAR2;

END PKG_SHAS_UTIL;
/

CREATE OR REPLACE PACKAGE BODY PKG_SHAS_UTIL AS

  -- ------------------------------------------------------------
  -- PRC_LOG: a transacao autonoma garante que o log seja salvo
  -- mesmo quando a transacao principal sofre ROLLBACK, e permite
  -- chama-lo de functions usadas em SELECT e de triggers.
  -- ------------------------------------------------------------
  PROCEDURE PRC_LOG (
    p_rotina   IN VARCHAR2,
    p_evento   IN VARCHAR2,
    p_mensagem IN VARCHAR2
  ) AS
    PRAGMA AUTONOMOUS_TRANSACTION;
  BEGIN
    INSERT INTO T_SHAS_LOG_PROCESSAMENTO (NM_ROTINA, TP_EVENTO, DS_MENSAGEM)
    VALUES (SUBSTR(p_rotina, 1, 60), p_evento, SUBSTR(p_mensagem, 1, 4000));
    COMMIT;
  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK;   -- o log nunca deve derrubar a rotina chamadora
  END PRC_LOG;

  -- ------------------------------------------------------------
  -- FN_CLASSIFICAR: MESMA regra do enum Classification (Java).
  --   HYPERTENSION : sis >= 140 ou dia >= 90
  --   ELEVATED     : sis 120-139 ou dia 80-89
  --   NORMAL       : sis < 120 e dia < 80
  -- Fase 7: a hipertensao passou a ser avaliada primeiro (antes, 141x85
  -- era classificada como Elevada - bug encontrado pelos testes automatizados).
  -- ------------------------------------------------------------
  FUNCTION FN_CLASSIFICAR (
    p_sistolica  IN NUMBER,
    p_diastolica IN NUMBER,
    p_formato    IN VARCHAR2 DEFAULT 'CODIGO'
  ) RETURN VARCHAR2 DETERMINISTIC
  AS
    v_codigo VARCHAR2(20);
  BEGIN
    IF p_sistolica IS NULL OR p_diastolica IS NULL THEN
      RAISE_APPLICATION_ERROR(-20003, 'Sistolica e diastolica sao obrigatorias.');
    END IF;

    IF p_sistolica >= c_meta_sistolica OR p_diastolica >= c_meta_diastolica THEN
      v_codigo := 'HYPERTENSION';
    ELSIF p_sistolica >= 120 OR p_diastolica >= 80 THEN
      v_codigo := 'ELEVATED';
    ELSE
      v_codigo := 'NORMAL';
    END IF;

    IF UPPER(p_formato) = 'ROTULO' THEN
      RETURN CASE v_codigo
               WHEN 'NORMAL'   THEN 'Normal'
               WHEN 'ELEVATED' THEN 'Elevada'
               ELSE 'Hipertensao'
             END;
    END IF;
    RETURN v_codigo;
  END FN_CLASSIFICAR;

  -- ------------------------------------------------------------
  -- FN_FORMATAR_MEDICAO (dados formatados)
  -- ------------------------------------------------------------
  FUNCTION FN_FORMATAR_MEDICAO (
    p_id_medicao IN NUMBER
  ) RETURN VARCHAR2
  AS
    v_med T_SHAS_MEDICAO%ROWTYPE;
  BEGIN
    SELECT * INTO v_med FROM T_SHAS_MEDICAO WHERE ID_MEDICAO = p_id_medicao;

    RETURN TO_CHAR(v_med.DT_MEDICAO, 'DD/MM/YYYY HH24:MI')
        || ' | ' || v_med.VL_SISTOLICA || 'x' || v_med.VL_DIASTOLICA || ' mmHg'
        || CASE WHEN v_med.VL_FREQ_CARDIACA IS NOT NULL
                THEN ' | FC ' || v_med.VL_FREQ_CARDIACA || ' bpm' END
        || ' | ' || FN_CLASSIFICAR(v_med.VL_SISTOLICA, v_med.VL_DIASTOLICA, 'ROTULO')
        || ' | ' || v_med.TP_ORIGEM;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      RAISE_APPLICATION_ERROR(-20004, 'Medicao ' || p_id_medicao || ' nao encontrada.');
  END FN_FORMATAR_MEDICAO;

END PKG_SHAS_UTIL;
/
