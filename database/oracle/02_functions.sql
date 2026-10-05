-- ============================================================
-- Smart HAS - Fase 6 | 02_functions.sql
-- Functions PL/SQL reutilizaveis. Todas usam parametros IN,
-- RETURN tipado, comentarios e tratamento de excecoes.
--
--  PRC_SHAS_LOG ............. (apoio) grava log em transacao autonoma
--  FN_SHAS_CLASSIFICAR ...... (apoio) classifica uma leitura
--  FN_SHAS_TAXA_CONTROLE .... INDICADOR: % de leituras na meta (<140/90)
--  FN_SHAS_RESUMO_PACIENTE .. DADOS FORMATADOS: texto-resumo do paciente
--  FN_SHAS_FORMATAR_MEDICAO . DADOS FORMATADOS: linha legivel de uma leitura
--
-- Codigos de erro do projeto (RAISE_APPLICATION_ERROR):
--  -20001 usuario inexistente  | -20002 periodo invalido
--  -20003 parametros nulos     | -20004 medicao inexistente
-- ============================================================

-- ------------------------------------------------------------
-- PRC_SHAS_LOG: grava um evento em T_SHAS_LOG_PROCESSAMENTO.
-- PRAGMA AUTONOMOUS_TRANSACTION garante que o log seja salvo
-- mesmo quando a transacao principal sofre ROLLBACK, e permite
-- chama-lo de dentro de functions usadas em SELECT.
-- ------------------------------------------------------------
CREATE OR REPLACE PROCEDURE PRC_SHAS_LOG (
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
END PRC_SHAS_LOG;
/

-- ------------------------------------------------------------
-- FN_SHAS_CLASSIFICAR
-- Classifica a pressao com a MESMA regra do back-end Java
-- (enum Classification), garantindo consistencia entre camadas.
--   NORMAL       : sis < 120 e dia < 80
--   ELEVATED     : sis 120-139 ou dia 80-89
--   HYPERTENSION : demais casos
-- p_formato = 'CODIGO' (padrao) ou 'ROTULO' (texto em portugues)
-- ------------------------------------------------------------
CREATE OR REPLACE FUNCTION FN_SHAS_CLASSIFICAR (
  p_sistolica  IN NUMBER,
  p_diastolica IN NUMBER,
  p_formato    IN VARCHAR2 DEFAULT 'CODIGO'
) RETURN VARCHAR2
DETERMINISTIC
AS
  v_codigo VARCHAR2(20);
BEGIN
  IF p_sistolica IS NULL OR p_diastolica IS NULL THEN
    RAISE_APPLICATION_ERROR(-20003, 'Sistolica e diastolica sao obrigatorias.');
  END IF;

  IF p_sistolica < 120 AND p_diastolica < 80 THEN
    v_codigo := 'NORMAL';
  ELSIF p_sistolica BETWEEN 120 AND 139 OR p_diastolica BETWEEN 80 AND 89 THEN
    v_codigo := 'ELEVATED';
  ELSE
    v_codigo := 'HYPERTENSION';
  END IF;

  IF UPPER(p_formato) = 'ROTULO' THEN
    RETURN CASE v_codigo
             WHEN 'NORMAL'   THEN 'Normal'
             WHEN 'ELEVATED' THEN 'Elevada'
             ELSE 'Hipertensao'
           END;
  END IF;
  RETURN v_codigo;
END FN_SHAS_CLASSIFICAR;
/

-- ------------------------------------------------------------
-- FN_SHAS_TAXA_CONTROLE  (INDICADOR)
-- Percentual de leituras do paciente dentro da meta terapeutica
-- (sistolica < 140 E diastolica < 90) nos ultimos p_dias dias.
-- Retorno: NUMBER com 1 casa decimal (0 a 100), ou NULL quando
-- o paciente nao tem leituras no periodo ("sem dados").
-- Uso: SELECT FN_SHAS_TAXA_CONTROLE(ID_USUARIO, 30) FROM ...
-- ------------------------------------------------------------
CREATE OR REPLACE FUNCTION FN_SHAS_TAXA_CONTROLE (
  p_id_usuario IN NUMBER,
  p_dias       IN NUMBER DEFAULT 30
) RETURN NUMBER
AS
  v_existe     NUMBER;
  v_total      NUMBER := 0;
  v_na_meta    NUMBER := 0;
BEGIN
  IF p_dias IS NULL OR p_dias <= 0 THEN
    RAISE_APPLICATION_ERROR(-20002, 'O periodo (p_dias) deve ser maior que zero.');
  END IF;

  -- Valida o usuario (dispara NO_DATA_FOUND se nao existir)
  SELECT 1 INTO v_existe FROM T_SHAS_USUARIO WHERE ID_USUARIO = p_id_usuario;

  SELECT COUNT(*),
         SUM(CASE WHEN VL_SISTOLICA < 140 AND VL_DIASTOLICA < 90 THEN 1 ELSE 0 END)
    INTO v_total, v_na_meta
    FROM T_SHAS_MEDICAO
   WHERE ID_USUARIO = p_id_usuario
     AND DT_MEDICAO >= SYSTIMESTAMP - NUMTODSINTERVAL(p_dias, 'DAY');

  IF v_total = 0 THEN
    RETURN NULL;
  END IF;

  RETURN ROUND(v_na_meta * 100 / v_total, 1);
EXCEPTION
  WHEN NO_DATA_FOUND THEN
    RAISE_APPLICATION_ERROR(-20001, 'Usuario ' || p_id_usuario || ' nao encontrado.');
END FN_SHAS_TAXA_CONTROLE;
/

-- ------------------------------------------------------------
-- FN_SHAS_FORMATAR_MEDICAO  (DADOS FORMATADOS)
-- Devolve uma linha legivel de uma leitura, ex.:
--   03/10/2026 07:15 | 145x95 mmHg | FC 82 bpm | Hipertensao | SENSOR
-- ------------------------------------------------------------
CREATE OR REPLACE FUNCTION FN_SHAS_FORMATAR_MEDICAO (
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
      || ' | ' || FN_SHAS_CLASSIFICAR(v_med.VL_SISTOLICA, v_med.VL_DIASTOLICA, 'ROTULO')
      || ' | ' || v_med.TP_ORIGEM;
EXCEPTION
  WHEN NO_DATA_FOUND THEN
    RAISE_APPLICATION_ERROR(-20004, 'Medicao ' || p_id_medicao || ' nao encontrada.');
END FN_SHAS_FORMATAR_MEDICAO;
/

-- ------------------------------------------------------------
-- FN_SHAS_RESUMO_PACIENTE  (DADOS FORMATADOS)
-- Texto-resumo pronto para exibicao no app/dashboard, ex.:
--   Paciente Demonstracao | 14 leituras em 30 dias | media 136/87 mmHg
--   | 57,1% na meta | ultima: 02/10/2026 07:10 (Hipertensao)
-- Reaproveita FN_SHAS_TAXA_CONTROLE e FN_SHAS_CLASSIFICAR.
-- ------------------------------------------------------------
CREATE OR REPLACE FUNCTION FN_SHAS_RESUMO_PACIENTE (
  p_id_usuario IN NUMBER,
  p_dias       IN NUMBER DEFAULT 30
) RETURN VARCHAR2
AS
  v_nome      T_SHAS_USUARIO.NM_COMPLETO%TYPE;
  v_qtd       NUMBER;
  v_media_sis NUMBER;
  v_media_dia NUMBER;
  v_taxa      NUMBER;
  v_ultima    VARCHAR2(80);
  v_texto     VARCHAR2(400);
BEGIN
  IF p_dias IS NULL OR p_dias <= 0 THEN
    RAISE_APPLICATION_ERROR(-20002, 'O periodo (p_dias) deve ser maior que zero.');
  END IF;

  SELECT NM_COMPLETO INTO v_nome FROM T_SHAS_USUARIO WHERE ID_USUARIO = p_id_usuario;

  SELECT COUNT(*), ROUND(AVG(VL_SISTOLICA)), ROUND(AVG(VL_DIASTOLICA))
    INTO v_qtd, v_media_sis, v_media_dia
    FROM T_SHAS_MEDICAO
   WHERE ID_USUARIO = p_id_usuario
     AND DT_MEDICAO >= SYSTIMESTAMP - NUMTODSINTERVAL(p_dias, 'DAY');

  IF v_qtd = 0 THEN
    RETURN v_nome || ' | nenhuma leitura nos ultimos ' || p_dias || ' dias';
  END IF;

  v_taxa := FN_SHAS_TAXA_CONTROLE(p_id_usuario, p_dias);

  -- Ultima leitura (subconsulta ordenada + ROWNUM: compativel com qualquer 11g+)
  SELECT TO_CHAR(DT_MEDICAO, 'DD/MM/YYYY HH24:MI') || ' ('
         || FN_SHAS_CLASSIFICAR(VL_SISTOLICA, VL_DIASTOLICA, 'ROTULO') || ')'
    INTO v_ultima
    FROM (SELECT DT_MEDICAO, VL_SISTOLICA, VL_DIASTOLICA
            FROM T_SHAS_MEDICAO
           WHERE ID_USUARIO = p_id_usuario
           ORDER BY DT_MEDICAO DESC)
   WHERE ROWNUM = 1;

  v_texto := v_nome
          || ' | ' || v_qtd || ' leituras em ' || p_dias || ' dias'
          || ' | media ' || v_media_sis || '/' || v_media_dia || ' mmHg'
          || ' | ' || TO_CHAR(v_taxa, 'FM990D0', 'NLS_NUMERIC_CHARACTERS='',.''') || '% na meta'
          || ' | ultima: ' || v_ultima;
  RETURN v_texto;
EXCEPTION
  WHEN NO_DATA_FOUND THEN
    RAISE_APPLICATION_ERROR(-20001, 'Usuario ' || p_id_usuario || ' nao encontrado.');
  WHEN OTHERS THEN
    -- Erros de usuario (-20xxx) sobem intactos; o resto e registrado
    IF SQLCODE BETWEEN -20999 AND -20000 THEN
      RAISE;
    END IF;
    PRC_SHAS_LOG('FN_SHAS_RESUMO_PACIENTE', 'ERRO',
                 'Usuario ' || p_id_usuario || ': ' || SQLERRM);
    RETURN 'Resumo indisponivel no momento';
END FN_SHAS_RESUMO_PACIENTE;
/
