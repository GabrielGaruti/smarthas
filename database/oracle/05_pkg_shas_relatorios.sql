-- ============================================================
-- Smart HAS - Fase 7 | 05_pkg_shas_relatorios.sql
-- PKG_SHAS_RELATORIOS: rotinas batch de consolidacao.
--
--  PRC_GERAR_RELATORIO_RESUMO ... relatorio consolidado por paciente
--      >>> acionada pelo painel admin (REST -> Java -> JDBC -> Oracle)
--
-- Fase 6 -> Fase 7: PRC_SHAS_GERAR_RELATORIO_RESUMO virou membro deste
-- package e passou a reaproveitar PKG_SHAS_INDICADORES.
--
-- Codigos de erro: -20011 periodo invalido
-- ============================================================

CREATE OR REPLACE PACKAGE PKG_SHAS_RELATORIOS AS

  -- Consolida os ultimos p_dias para todos os pacientes (p_id_usuario NULL)
  -- ou para um paciente. Saida: quantidade de relatorios gravados.
  PROCEDURE PRC_GERAR_RELATORIO_RESUMO (
    p_dias        IN  NUMBER DEFAULT 30,
    p_id_usuario  IN  NUMBER DEFAULT NULL,
    p_qt_gerados  OUT NUMBER
  );

  -- Classificacao de risco usada pelo relatorio (exposta para reuso/testes).
  --   SEM DADOS : nenhuma leitura | ALTO : critico aberto ou taxa < 50%
  --   MODERADO  : taxa < 80%       | BAIXO : demais casos
  FUNCTION FN_NIVEL_RISCO (
    p_qt_medicoes IN NUMBER,
    p_taxa        IN NUMBER,
    p_qt_criticos IN NUMBER
  ) RETURN VARCHAR2 DETERMINISTIC;

END PKG_SHAS_RELATORIOS;
/

CREATE OR REPLACE PACKAGE BODY PKG_SHAS_RELATORIOS AS

  c_rotina CONSTANT VARCHAR2(50) := 'PKG_SHAS_RELATORIOS.PRC_GERAR_RELATORIO_RESUMO';

  FUNCTION FN_NIVEL_RISCO (
    p_qt_medicoes IN NUMBER,
    p_taxa        IN NUMBER,
    p_qt_criticos IN NUMBER
  ) RETURN VARCHAR2 DETERMINISTIC
  AS
  BEGIN
    IF NVL(p_qt_medicoes, 0) = 0 THEN
      RETURN 'SEM DADOS';
    ELSIF NVL(p_qt_criticos, 0) > 0 OR p_taxa < 50 THEN
      RETURN 'ALTO';
    ELSIF p_taxa < 80 THEN
      RETURN 'MODERADO';
    END IF;
    RETURN 'BAIXO';
  END FN_NIVEL_RISCO;

  -- ------------------------------------------------------------
  -- PRC_GERAR_RELATORIO_RESUMO
  -- CURSOR FOR LOOP por paciente, tratamento de erro POR PACIENTE
  -- (uma falha nao interrompe os demais), COMMIT ao final e log.
  -- ------------------------------------------------------------
  PROCEDURE PRC_GERAR_RELATORIO_RESUMO (
    p_dias        IN  NUMBER DEFAULT 30,
    p_id_usuario  IN  NUMBER DEFAULT NULL,
    p_qt_gerados  OUT NUMBER
  ) AS
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

        v_taxa  := PKG_SHAS_INDICADORES.FN_TAXA_CONTROLE(r.ID_USUARIO, p_dias);
        v_risco := FN_NIVEL_RISCO(v_qtd, v_taxa, v_criticos);

        INSERT INTO T_SHAS_RELATORIO_RESUMO (
          ID_USUARIO, DT_INICIO, DT_FIM, QT_MEDICOES, VL_MEDIA_SISTOLICA, VL_MEDIA_DIASTOLICA,
          VL_MAX_SISTOLICA, PC_CONTROLE, QT_ALERTAS_ABERTOS, NV_RISCO, DS_RESUMO)
        VALUES (
          r.ID_USUARIO, v_inicio, TRUNC(SYSDATE), v_qtd, v_media_sis, v_media_dia,
          v_max_sis, v_taxa, v_alertas, v_risco,
          SUBSTR(PKG_SHAS_INDICADORES.FN_RESUMO_PACIENTE(r.ID_USUARIO, p_dias), 1, 400));

        p_qt_gerados := p_qt_gerados + 1;
        DBMS_OUTPUT.PUT_LINE(RPAD(r.NM_COMPLETO, 30) || ' -> risco ' || v_risco
                             || ' | taxa ' || NVL(TO_CHAR(v_taxa), '-') || '%');
      EXCEPTION
        WHEN OTHERS THEN
          v_falhas := v_falhas + 1;
          PKG_SHAS_UTIL.PRC_LOG(c_rotina, PKG_SHAS_UTIL.c_erro, 'Paciente ' || r.ID_USUARIO || ': ' || SQLERRM);
      END;
    END LOOP;

    COMMIT;
    PKG_SHAS_UTIL.PRC_LOG(c_rotina,
                          CASE WHEN v_falhas = 0 THEN PKG_SHAS_UTIL.c_info ELSE PKG_SHAS_UTIL.c_aviso END,
                          p_qt_gerados || ' relatorio(s) gerado(s), ' || v_falhas
                          || ' falha(s), periodo de ' || p_dias || ' dias.');
  EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK;
      PKG_SHAS_UTIL.PRC_LOG(c_rotina, PKG_SHAS_UTIL.c_erro, SQLERRM);
      RAISE;
  END PRC_GERAR_RELATORIO_RESUMO;

END PKG_SHAS_RELATORIOS;
/
