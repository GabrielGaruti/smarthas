-- ============================================================
-- Smart HAS - Fase 7 | 04_pkg_shas_alertas.sql
-- PKG_SHAS_ALERTAS: regra clinica de alertas e entrada de dados IoT.
--
--  PRC_REGISTRAR_ALERTA ........ avalia UMA leitura e gera alerta
--      >>> acionada pelo back-end Java (evento "medicao registrada")
--  PRC_SIMULAR_LEITURAS_SENSOR . importa leituras simuladas de IoT
--  PRC_RESOLVER_ALERTAS_PACIENTE encerra os alertas abertos de um paciente
--
-- Fase 6 -> Fase 7: PRC_SHAS_REGISTRAR_ALERTA e
-- PRC_SHAS_SIMULAR_LEITURAS_SENSOR viraram membros deste package.
-- A atualizacao de DT_ULTIMA_SYNC do dispositivo passou para a
-- trigger TRG_SHAS_MEDICAO_SYNC_DISP, e DT_RESOLUCAO do alerta e
-- preenchida pela trigger TRG_SHAS_ALERTA_STATUS.
--
-- Codigos de erro: -20010 medicao inexistente | -20012 dispositivo inexistente/inativo
--                  -20013 quantidade invalida | -20001 usuario inexistente
-- ============================================================

CREATE OR REPLACE PACKAGE PKG_SHAS_ALERTAS AS

  -- Avalia a leitura e grava o alerta (p_id_alerta NULL = sem alerta).
  -- Nao executa COMMIT: quem chama controla a transacao.
  PROCEDURE PRC_REGISTRAR_ALERTA (
    p_id_medicao  IN  NUMBER,
    p_id_alerta   OUT NUMBER,
    p_severidade  OUT VARCHAR2
  );

  -- Gera p_quantidade leituras plausiveis para o dispositivo nos ultimos p_dias.
  PROCEDURE PRC_SIMULAR_LEITURAS_SENSOR (
    p_id_dispositivo IN  NUMBER,
    p_quantidade     IN  NUMBER,
    p_dias           IN  NUMBER DEFAULT 30,
    p_qt_inseridas   OUT NUMBER
  );

  -- Encerra todos os alertas ABERTOS do paciente (ex.: apos consulta medica).
  PROCEDURE PRC_RESOLVER_ALERTAS_PACIENTE (
    p_id_usuario    IN  NUMBER,
    p_qt_resolvidos OUT NUMBER
  );

END PKG_SHAS_ALERTAS;
/

CREATE OR REPLACE PACKAGE BODY PKG_SHAS_ALERTAS AS

  c_rotina CONSTANT VARCHAR2(30) := 'PKG_SHAS_ALERTAS';

  -- ------------------------------------------------------------
  -- PRC_REGISTRAR_ALERTA
  -- Regras (IF/ELSIF):
  --   1) sis >= 180 ou dia >= 120 ......... CRITICO  / CRISE_HIPERTENSIVA
  --   2) sis >= 140 ou dia >= 90 e, entre as 5 ultimas leituras,
  --      3 ou mais sao hipertensao (CURSOR + LOOP) ... ALTO / TENDENCIA_ALTA
  --   3) sis >= 140 ou dia >= 90 ............ MODERADO / HIPERTENSAO
  --   4) demais ............................. nenhum alerta
  -- Idempotente: reprocessar a mesma leitura devolve o alerta ja existente
  -- (constraint UK_SHAS_ALERTA_MED_TIPO + DUP_VAL_ON_INDEX).
  -- ------------------------------------------------------------
  PROCEDURE PRC_REGISTRAR_ALERTA (
    p_id_medicao  IN  NUMBER,
    p_id_alerta   OUT NUMBER,
    p_severidade  OUT VARCHAR2
  ) AS
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

    ELSIF v_sis >= PKG_SHAS_UTIL.c_meta_sistolica OR v_dia >= PKG_SHAS_UTIL.c_meta_diastolica THEN
      OPEN c_recentes(v_id_usuario, v_dt_medicao);
      LOOP
        FETCH c_recentes INTO r_rec;
        EXIT WHEN c_recentes%NOTFOUND;
        IF PKG_SHAS_UTIL.FN_CLASSIFICAR(r_rec.VL_SISTOLICA, r_rec.VL_DIASTOLICA) = 'HYPERTENSION' THEN
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
      PKG_SHAS_UTIL.PRC_LOG(c_rotina || '.PRC_REGISTRAR_ALERTA', PKG_SHAS_UTIL.c_aviso,
                            'Medicao ' || p_id_medicao || ' nao encontrada.');
      RAISE_APPLICATION_ERROR(-20010, 'Medicao ' || p_id_medicao || ' nao encontrada.');
    WHEN OTHERS THEN
      IF c_recentes%ISOPEN THEN
        CLOSE c_recentes;
      END IF;
      PKG_SHAS_UTIL.PRC_LOG(c_rotina || '.PRC_REGISTRAR_ALERTA', PKG_SHAS_UTIL.c_erro,
                            'Medicao ' || p_id_medicao || ': ' || SQLERRM);
      RAISE;
  END PRC_REGISTRAR_ALERTA;

  -- ------------------------------------------------------------
  -- PRC_SIMULAR_LEITURAS_SENSOR
  -- Cada leitura inserida passa por PRC_REGISTRAR_ALERTA (reuso da
  -- regra). A trigger TRG_SHAS_MEDICAO_SYNC_DISP atualiza a data de
  -- sincronizacao do dispositivo automaticamente.
  -- ------------------------------------------------------------
  PROCEDURE PRC_SIMULAR_LEITURAS_SENSOR (
    p_id_dispositivo IN  NUMBER,
    p_quantidade     IN  NUMBER,
    p_dias           IN  NUMBER DEFAULT 30,
    p_qt_inseridas   OUT NUMBER
  ) AS
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
      v_dia := LEAST(ROUND(DBMS_RANDOM.VALUE(65, 108)), v_sis - 25);  -- coerencia fisiologica
      v_fc  := ROUND(DBMS_RANDOM.VALUE(58, 104));

      INSERT INTO T_SHAS_MEDICAO (ID_USUARIO, ID_DISPOSITIVO, VL_SISTOLICA, VL_DIASTOLICA,
                                  VL_FREQ_CARDIACA, DT_MEDICAO, TP_ORIGEM, DS_OBSERVACAO)
      VALUES (v_id_usuario, p_id_dispositivo, v_sis, v_dia, v_fc,
              CAST(SYSTIMESTAMP AS TIMESTAMP) - NUMTODSINTERVAL(DBMS_RANDOM.VALUE(0, NVL(p_dias, 30)), 'DAY'),
              'SENSOR', 'Leitura automatica do dispositivo ' || p_id_dispositivo)
      RETURNING ID_MEDICAO INTO v_id_medicao;

      PRC_REGISTRAR_ALERTA(v_id_medicao, v_id_alerta, v_sev);
      p_qt_inseridas := p_qt_inseridas + 1;
    END LOOP;

    COMMIT;
    PKG_SHAS_UTIL.PRC_LOG(c_rotina || '.PRC_SIMULAR_LEITURAS_SENSOR', PKG_SHAS_UTIL.c_info,
                          p_qt_inseridas || ' leitura(s) importada(s) do dispositivo ' || p_id_dispositivo);
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      ROLLBACK;
      RAISE_APPLICATION_ERROR(-20012, 'Dispositivo ' || p_id_dispositivo || ' nao encontrado.');
    WHEN OTHERS THEN
      ROLLBACK;
      PKG_SHAS_UTIL.PRC_LOG(c_rotina || '.PRC_SIMULAR_LEITURAS_SENSOR', PKG_SHAS_UTIL.c_erro,
                            'Dispositivo ' || p_id_dispositivo || ': ' || SQLERRM);
      RAISE;
  END PRC_SIMULAR_LEITURAS_SENSOR;

  -- ------------------------------------------------------------
  -- PRC_RESOLVER_ALERTAS_PACIENTE
  -- UPDATE em lote; DT_RESOLUCAO e auditoria ficam por conta das
  -- triggers (TRG_SHAS_ALERTA_STATUS e TRG_SHAS_ALERTA_AUDITORIA).
  -- ------------------------------------------------------------
  PROCEDURE PRC_RESOLVER_ALERTAS_PACIENTE (
    p_id_usuario    IN  NUMBER,
    p_qt_resolvidos OUT NUMBER
  ) AS
    v_existe NUMBER;
  BEGIN
    SELECT 1 INTO v_existe FROM T_SHAS_USUARIO WHERE ID_USUARIO = p_id_usuario;

    UPDATE T_SHAS_ALERTA
       SET ST_ALERTA = 'RESOLVIDO'
     WHERE ID_USUARIO = p_id_usuario
       AND ST_ALERTA = 'ABERTO';
    p_qt_resolvidos := SQL%ROWCOUNT;

    PKG_SHAS_UTIL.PRC_LOG(c_rotina || '.PRC_RESOLVER_ALERTAS_PACIENTE', PKG_SHAS_UTIL.c_info,
                          p_qt_resolvidos || ' alerta(s) resolvido(s) do usuario ' || p_id_usuario);
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      RAISE_APPLICATION_ERROR(-20001, 'Usuario ' || p_id_usuario || ' nao encontrado.');
  END PRC_RESOLVER_ALERTAS_PACIENTE;

END PKG_SHAS_ALERTAS;
/
