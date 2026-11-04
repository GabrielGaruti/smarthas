-- ============================================================
-- Smart HAS - Fase 7 | 03_pkg_shas_indicadores.sql
-- PKG_SHAS_INDICADORES: indicadores clinicos e consultas do painel.
--
--  FN_TAXA_CONTROLE ............ INDICADOR: % de leituras na meta (<140/90)
--  FN_RESUMO_PACIENTE .......... DADOS FORMATADOS: texto-resumo do paciente
--  PRC_CONSULTAR_MEDICOES ...... DYNAMIC SQL: busca com filtros opcionais,
--                                ordenacao e limite escolhidos em tempo real
--  FN_ESTATISTICA_POR_DIMENSAO . DYNAMIC SQL: agrupamento por uma dimensao
--                                escolhida em tempo real (GROUP BY dinamico)
--
-- Seguranca do SQL dinamico: valores do usuario entram SEMPRE como bind
-- variables (USING); nomes de colunas/expressoes vem de listas fechadas
-- (whitelist) definidas no proprio package. Nada do usuario e concatenado.
--
-- Codigos de erro:
--  -20001 usuario inexistente     | -20002 periodo invalido
--  -20020 filtro invalido         | -20021 ordenacao/dimensao invalida
--  -20022 limite fora de 1 a 500
-- ============================================================

CREATE OR REPLACE PACKAGE PKG_SHAS_INDICADORES AS

  -- % de leituras do paciente na meta nos ultimos p_dias (NULL = sem dados).
  FUNCTION FN_TAXA_CONTROLE (
    p_id_usuario IN NUMBER,
    p_dias       IN NUMBER DEFAULT 30
  ) RETURN NUMBER;

  -- Texto pronto para o app e o painel.
  FUNCTION FN_RESUMO_PACIENTE (
    p_id_usuario IN NUMBER,
    p_dias       IN NUMBER DEFAULT 30
  ) RETURN VARCHAR2;

  -- Busca dinamica de medicoes. Todos os filtros sao opcionais (NULL = ignorar).
  --   p_classificacao : NORMAL | ELEVATED | HYPERTENSION
  --   p_origem        : MANUAL | SENSOR
  --   p_ordenar_por   : DATA | SISTOLICA | DIASTOLICA | PACIENTE
  --   p_direcao       : ASC | DESC
  -- Saidas: p_total (total sem o limite), p_sql (comando gerado, para
  -- auditoria/demonstracao) e p_cursor (linhas encontradas).
  PROCEDURE PRC_CONSULTAR_MEDICOES (
    p_id_usuario     IN  NUMBER   DEFAULT NULL,
    p_classificacao  IN  VARCHAR2 DEFAULT NULL,
    p_origem         IN  VARCHAR2 DEFAULT NULL,
    p_dias           IN  NUMBER   DEFAULT NULL,
    p_sistolica_min  IN  NUMBER   DEFAULT NULL,
    p_ordenar_por    IN  VARCHAR2 DEFAULT 'DATA',
    p_direcao        IN  VARCHAR2 DEFAULT 'DESC',
    p_limite         IN  NUMBER   DEFAULT 50,
    p_total          OUT NUMBER,
    p_sql            OUT VARCHAR2,
    p_cursor         OUT SYS_REFCURSOR
  );

  -- Estatistica agrupada por uma dimensao escolhida em tempo real:
  --   CLASSIFICACAO | ORIGEM | PACIENTE | PERIODO_DIA
  -- Colunas: DS_GRUPO, QT_MEDICOES, VL_MEDIA_SISTOLICA, VL_MEDIA_DIASTOLICA, PC_NA_META
  FUNCTION FN_ESTATISTICA_POR_DIMENSAO (
    p_dimensao IN VARCHAR2,
    p_dias     IN NUMBER DEFAULT 30
  ) RETURN SYS_REFCURSOR;

END PKG_SHAS_INDICADORES;
/

CREATE OR REPLACE PACKAGE BODY PKG_SHAS_INDICADORES AS

  c_rotina CONSTANT VARCHAR2(30) := 'PKG_SHAS_INDICADORES';

  -- Valida o periodo (compartilhado pelas rotinas do package)
  PROCEDURE validar_dias (p_dias IN NUMBER) IS
  BEGIN
    IF p_dias IS NULL OR p_dias <= 0 THEN
      RAISE_APPLICATION_ERROR(-20002, 'O periodo (p_dias) deve ser maior que zero.');
    END IF;
  END validar_dias;

  -- ------------------------------------------------------------
  -- FN_TAXA_CONTROLE (INDICADOR)
  -- ------------------------------------------------------------
  FUNCTION FN_TAXA_CONTROLE (
    p_id_usuario IN NUMBER,
    p_dias       IN NUMBER DEFAULT 30
  ) RETURN NUMBER
  AS
    v_existe  NUMBER;
    v_total   NUMBER := 0;
    v_na_meta NUMBER := 0;
  BEGIN
    validar_dias(p_dias);

    -- Valida o usuario (dispara NO_DATA_FOUND se nao existir)
    SELECT 1 INTO v_existe FROM T_SHAS_USUARIO WHERE ID_USUARIO = p_id_usuario;

    SELECT COUNT(*),
           SUM(CASE WHEN VL_SISTOLICA  < PKG_SHAS_UTIL.c_meta_sistolica
                     AND VL_DIASTOLICA < PKG_SHAS_UTIL.c_meta_diastolica THEN 1 ELSE 0 END)
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
  END FN_TAXA_CONTROLE;

  -- ------------------------------------------------------------
  -- FN_RESUMO_PACIENTE (DADOS FORMATADOS)
  --   Paciente Demonstracao | 14 leituras em 30 dias | media 136/87 mmHg
  --   | 57,1% na meta | ultima: 02/10/2026 07:10 (Hipertensao)
  -- ------------------------------------------------------------
  FUNCTION FN_RESUMO_PACIENTE (
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
  BEGIN
    validar_dias(p_dias);

    SELECT NM_COMPLETO INTO v_nome FROM T_SHAS_USUARIO WHERE ID_USUARIO = p_id_usuario;

    SELECT COUNT(*), ROUND(AVG(VL_SISTOLICA)), ROUND(AVG(VL_DIASTOLICA))
      INTO v_qtd, v_media_sis, v_media_dia
      FROM T_SHAS_MEDICAO
     WHERE ID_USUARIO = p_id_usuario
       AND DT_MEDICAO >= SYSTIMESTAMP - NUMTODSINTERVAL(p_dias, 'DAY');

    IF v_qtd = 0 THEN
      RETURN v_nome || ' | nenhuma leitura nos ultimos ' || p_dias || ' dias';
    END IF;

    v_taxa := FN_TAXA_CONTROLE(p_id_usuario, p_dias);

    SELECT TO_CHAR(DT_MEDICAO, 'DD/MM/YYYY HH24:MI') || ' ('
           || PKG_SHAS_UTIL.FN_CLASSIFICAR(VL_SISTOLICA, VL_DIASTOLICA, 'ROTULO') || ')'
      INTO v_ultima
      FROM (SELECT DT_MEDICAO, VL_SISTOLICA, VL_DIASTOLICA
              FROM T_SHAS_MEDICAO
             WHERE ID_USUARIO = p_id_usuario
             ORDER BY DT_MEDICAO DESC)
     WHERE ROWNUM = 1;

    RETURN v_nome
        || ' | ' || v_qtd || ' leituras em ' || p_dias || ' dias'
        || ' | media ' || v_media_sis || '/' || v_media_dia || ' mmHg'
        || ' | ' || TO_CHAR(v_taxa, 'FM990D0', 'NLS_NUMERIC_CHARACTERS='',.''') || '% na meta'
        || ' | ultima: ' || v_ultima;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      RAISE_APPLICATION_ERROR(-20001, 'Usuario ' || p_id_usuario || ' nao encontrado.');
    WHEN OTHERS THEN
      -- Erros de usuario (-20xxx) sobem intactos; o resto e registrado
      IF SQLCODE BETWEEN -20999 AND -20000 THEN
        RAISE;
      END IF;
      PKG_SHAS_UTIL.PRC_LOG(c_rotina || '.FN_RESUMO_PACIENTE', PKG_SHAS_UTIL.c_erro,
                            'Usuario ' || p_id_usuario || ': ' || SQLERRM);
      RETURN 'Resumo indisponivel no momento';
  END FN_RESUMO_PACIENTE;

  -- ------------------------------------------------------------
  -- PRC_CONSULTAR_MEDICOES (DYNAMIC SQL)
  -- O painel administrativo combina filtros livremente; montar uma
  -- consulta estatica para cada combinacao seria inviavel. O comando
  -- e montado em tempo de execucao apenas com os filtros informados.
  --
  -- Tecnica de bind de quantidade fixa: filtro ausente vira
  -- "(1 = 1 OR :b IS NULL)", que o otimizador elimina, mas mantem a
  -- mesma lista de binds no USING (OPEN ... FOR e EXECUTE IMMEDIATE).
  -- ------------------------------------------------------------
  PROCEDURE PRC_CONSULTAR_MEDICOES (
    p_id_usuario     IN  NUMBER   DEFAULT NULL,
    p_classificacao  IN  VARCHAR2 DEFAULT NULL,
    p_origem         IN  VARCHAR2 DEFAULT NULL,
    p_dias           IN  NUMBER   DEFAULT NULL,
    p_sistolica_min  IN  NUMBER   DEFAULT NULL,
    p_ordenar_por    IN  VARCHAR2 DEFAULT 'DATA',
    p_direcao        IN  VARCHAR2 DEFAULT 'DESC',
    p_limite         IN  NUMBER   DEFAULT 50,
    p_total          OUT NUMBER,
    p_sql            OUT VARCHAR2,
    p_cursor         OUT SYS_REFCURSOR
  ) AS
    v_classif  VARCHAR2(20) := UPPER(TRIM(p_classificacao));
    v_origem   VARCHAR2(10) := UPPER(TRIM(p_origem));
    v_from     VARCHAR2(200);
    v_where    VARCHAR2(1000) := ' WHERE 1 = 1';
    v_order    VARCHAR2(100);
    v_select   VARCHAR2(1000);

    -- Acrescenta um filtro real ou o "filtro neutro" que preserva o bind
    PROCEDURE add_filtro (p_ativo BOOLEAN, p_condicao VARCHAR2, p_bind VARCHAR2) IS
    BEGIN
      IF p_ativo THEN
        v_where := v_where || ' AND ' || p_condicao;
      ELSE
        v_where := v_where || ' AND (1 = 1 OR ' || p_bind || ' IS NULL)';
      END IF;
    END add_filtro;
  BEGIN
    -- 1) Validacao dos parametros
    IF v_classif IS NOT NULL AND v_classif NOT IN ('NORMAL','ELEVATED','HYPERTENSION') THEN
      RAISE_APPLICATION_ERROR(-20020, 'Classificacao invalida: use NORMAL, ELEVATED ou HYPERTENSION.');
    END IF;
    IF v_origem IS NOT NULL AND v_origem NOT IN ('MANUAL','SENSOR') THEN
      RAISE_APPLICATION_ERROR(-20020, 'Origem invalida: use MANUAL ou SENSOR.');
    END IF;
    IF p_dias IS NOT NULL THEN
      validar_dias(p_dias);
    END IF;
    IF p_limite IS NULL OR p_limite NOT BETWEEN 1 AND 500 THEN
      RAISE_APPLICATION_ERROR(-20022, 'Limite deve estar entre 1 e 500.');
    END IF;

    -- 2) ORDER BY por whitelist (nunca concatena texto vindo do usuario)
    v_order := CASE UPPER(NVL(p_ordenar_por, 'DATA'))
                 WHEN 'DATA'       THEN 'm.DT_MEDICAO'
                 WHEN 'SISTOLICA'  THEN 'm.VL_SISTOLICA'
                 WHEN 'DIASTOLICA' THEN 'm.VL_DIASTOLICA'
                 WHEN 'PACIENTE'   THEN 'u.NM_COMPLETO'
               END;
    IF v_order IS NULL OR UPPER(NVL(p_direcao, 'DESC')) NOT IN ('ASC','DESC') THEN
      RAISE_APPLICATION_ERROR(-20021, 'Ordenacao invalida: campo DATA/SISTOLICA/DIASTOLICA/PACIENTE e direcao ASC/DESC.');
    END IF;
    v_order := ' ORDER BY ' || v_order || ' ' || UPPER(NVL(p_direcao, 'DESC')) || ', m.ID_MEDICAO DESC';

    -- 3) Monta o WHERE apenas com os filtros informados
    v_from := ' FROM T_SHAS_MEDICAO m JOIN T_SHAS_USUARIO u ON u.ID_USUARIO = m.ID_USUARIO';
    add_filtro(p_id_usuario IS NOT NULL,    'm.ID_USUARIO = :b_usuario', ':b_usuario');
    add_filtro(v_classif IS NOT NULL,
               'PKG_SHAS_UTIL.FN_CLASSIFICAR(m.VL_SISTOLICA, m.VL_DIASTOLICA) = :b_classif', ':b_classif');
    add_filtro(v_origem IS NOT NULL,        'm.TP_ORIGEM = :b_origem', ':b_origem');
    add_filtro(p_dias IS NOT NULL,
               'm.DT_MEDICAO >= SYSTIMESTAMP - NUMTODSINTERVAL(:b_dias, ''DAY'')', ':b_dias');
    add_filtro(p_sistolica_min IS NOT NULL, 'm.VL_SISTOLICA >= :b_sis_min', ':b_sis_min');

    -- 4) Total de registros (EXECUTE IMMEDIATE ... INTO ... USING)
    EXECUTE IMMEDIATE 'SELECT COUNT(*)' || v_from || v_where
       INTO p_total
      USING p_id_usuario, v_classif, v_origem, p_dias, p_sistolica_min;

    -- 5) Linhas da pagina (OPEN ... FOR ... USING devolve um REF CURSOR ao Java)
    v_select := 'SELECT m.ID_MEDICAO, m.ID_USUARIO, u.NM_COMPLETO, m.VL_SISTOLICA, m.VL_DIASTOLICA,'
             || ' m.VL_FREQ_CARDIACA, m.DT_MEDICAO, m.TP_ORIGEM,'
             || ' PKG_SHAS_UTIL.FN_CLASSIFICAR(m.VL_SISTOLICA, m.VL_DIASTOLICA) AS CD_CLASSIFICACAO';
    p_sql := v_select || v_from || v_where || v_order || ' FETCH FIRST :b_limite ROWS ONLY';

    OPEN p_cursor FOR p_sql
      USING p_id_usuario, v_classif, v_origem, p_dias, p_sistolica_min, p_limite;

    PKG_SHAS_UTIL.PRC_LOG(c_rotina || '.PRC_CONSULTAR_MEDICOES', PKG_SHAS_UTIL.c_info,
        'Consulta dinamica: ' || p_total || ' registro(s) | usuario=' || NVL(TO_CHAR(p_id_usuario), '*')
        || ' classif=' || NVL(v_classif, '*') || ' origem=' || NVL(v_origem, '*')
        || ' dias=' || NVL(TO_CHAR(p_dias), '*') || ' sis_min=' || NVL(TO_CHAR(p_sistolica_min), '*'));
  EXCEPTION
    WHEN OTHERS THEN
      IF p_cursor%ISOPEN THEN
        CLOSE p_cursor;
      END IF;
      IF SQLCODE NOT BETWEEN -20999 AND -20000 THEN
        PKG_SHAS_UTIL.PRC_LOG(c_rotina || '.PRC_CONSULTAR_MEDICOES', PKG_SHAS_UTIL.c_erro,
                              SQLERRM || ' | SQL: ' || p_sql);
      END IF;
      RAISE;
  END PRC_CONSULTAR_MEDICOES;

  -- ------------------------------------------------------------
  -- FN_ESTATISTICA_POR_DIMENSAO (DYNAMIC SQL)
  -- O GROUP BY muda conforme a dimensao pedida pelo painel. A
  -- expressao vem de uma whitelist; o periodo entra por bind.
  -- ------------------------------------------------------------
  FUNCTION FN_ESTATISTICA_POR_DIMENSAO (
    p_dimensao IN VARCHAR2,
    p_dias     IN NUMBER DEFAULT 30
  ) RETURN SYS_REFCURSOR
  AS
    v_expr   VARCHAR2(300);
    v_sql    VARCHAR2(2000);
    v_cursor SYS_REFCURSOR;
  BEGIN
    validar_dias(p_dias);

    v_expr := CASE UPPER(TRIM(p_dimensao))
                WHEN 'CLASSIFICACAO' THEN 'PKG_SHAS_UTIL.FN_CLASSIFICAR(m.VL_SISTOLICA, m.VL_DIASTOLICA, ''ROTULO'')'
                WHEN 'ORIGEM'        THEN 'm.TP_ORIGEM'
                WHEN 'PACIENTE'      THEN 'u.NM_COMPLETO'
                WHEN 'PERIODO_DIA'   THEN 'CASE WHEN EXTRACT(HOUR FROM m.DT_MEDICAO) < 12 THEN ''Manha'''
                                       || ' WHEN EXTRACT(HOUR FROM m.DT_MEDICAO) < 18 THEN ''Tarde'''
                                       || ' ELSE ''Noite'' END'
              END;
    IF v_expr IS NULL THEN
      RAISE_APPLICATION_ERROR(-20021, 'Dimensao invalida: use CLASSIFICACAO, ORIGEM, PACIENTE ou PERIODO_DIA.');
    END IF;

    v_sql := 'SELECT ' || v_expr || ' AS DS_GRUPO,'
          || ' COUNT(*) AS QT_MEDICOES,'
          || ' ROUND(AVG(m.VL_SISTOLICA), 1) AS VL_MEDIA_SISTOLICA,'
          || ' ROUND(AVG(m.VL_DIASTOLICA), 1) AS VL_MEDIA_DIASTOLICA,'
          || ' ROUND(100 * SUM(CASE WHEN m.VL_SISTOLICA < ' || PKG_SHAS_UTIL.c_meta_sistolica
          ||                     ' AND m.VL_DIASTOLICA < ' || PKG_SHAS_UTIL.c_meta_diastolica
          ||                     ' THEN 1 ELSE 0 END) / COUNT(*), 1) AS PC_NA_META'
          || ' FROM T_SHAS_MEDICAO m JOIN T_SHAS_USUARIO u ON u.ID_USUARIO = m.ID_USUARIO'
          || ' WHERE m.DT_MEDICAO >= SYSTIMESTAMP - NUMTODSINTERVAL(:b_dias, ''DAY'')'
          || ' GROUP BY ' || v_expr
          || ' ORDER BY QT_MEDICOES DESC, DS_GRUPO';

    OPEN v_cursor FOR v_sql USING p_dias;
    RETURN v_cursor;
  END FN_ESTATISTICA_POR_DIMENSAO;

END PKG_SHAS_INDICADORES;
/
