-- ============================================================
-- Smart HAS - Fase 7 | 06_triggers.sql
-- Triggers de auditoria e automacao.
--
--  TRG_SHAS_ALERTA_AUDITORIA ... AUDITORIA: grava todo INSERT/UPDATE/DELETE
--                                de alertas em T_SHAS_AUDITORIA_ALERTA e
--                                registra no log cada alerta CRITICO criado
--  TRG_SHAS_ALERTA_STATUS ...... AUTOMACAO: preenche/limpa DT_RESOLUCAO
--                                conforme o status do alerta muda
--  TRG_SHAS_MEDICAO_DISPOSITIVO  AUTOMACAO + INTEGRIDADE: leitura enviada por
--                                dispositivo IoT so e aceita se o aparelho
--                                estiver ativo e for do proprio paciente;
--                                marca a origem como SENSOR e atualiza a
--                                data da ultima sincronizacao do aparelho
--
-- Por que o alerta NAO e gerado por trigger em T_SHAS_MEDICAO?
-- A regra precisa ler as ultimas leituras da propria T_SHAS_MEDICAO, o que
-- causaria ORA-04091 (tabela mutante). Por isso o alerta continua sendo
-- disparado pelo evento de back-end (AFTER_COMMIT) que chama
-- PKG_SHAS_ALERTAS.PRC_REGISTRAR_ALERTA.
--
-- Codigos de erro: -20030 dispositivo de outro paciente | -20031 dispositivo inativo
--                  -20032 dispositivo inexistente
-- ============================================================

-- ------------------------------------------------------------
-- TRG_SHAS_ALERTA_AUDITORIA (AFTER ... FOR EACH ROW)
-- Usa os predicados INSERTING / UPDATING / DELETING para saber a
-- operacao e guarda status anterior e novo. DS_ORIGEM identifica o
-- programa que alterou o dado (ex.: "JDBC Thin Client" = API Java,
-- "SQL Developer", "SQL*Plus").
-- ------------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_SHAS_ALERTA_AUDITORIA
AFTER INSERT OR UPDATE OR DELETE ON T_SHAS_ALERTA
FOR EACH ROW
DECLARE
  v_operacao T_SHAS_AUDITORIA_ALERTA.TP_OPERACAO%TYPE;
  v_origem   T_SHAS_AUDITORIA_ALERTA.DS_ORIGEM%TYPE;
BEGIN
  v_operacao := CASE
                  WHEN INSERTING THEN 'INSERT'
                  WHEN UPDATING  THEN 'UPDATE'
                  ELSE 'DELETE'
                END;
  v_origem := SUBSTR(NVL(SYS_CONTEXT('USERENV', 'MODULE'), 'desconhecido')
                     || ' @ ' || NVL(SYS_CONTEXT('USERENV', 'HOST'), '-'), 1, 200);

  INSERT INTO T_SHAS_AUDITORIA_ALERTA (
    ID_ALERTA, ID_USUARIO, TP_OPERACAO, ST_ANTERIOR, ST_NOVO, NV_SEVERIDADE, DS_ORIGEM)
  VALUES (
    NVL(:NEW.ID_ALERTA, :OLD.ID_ALERTA),
    NVL(:NEW.ID_USUARIO, :OLD.ID_USUARIO),
    v_operacao,
    :OLD.ST_ALERTA,
    :NEW.ST_ALERTA,
    NVL(:NEW.NV_SEVERIDADE, :OLD.NV_SEVERIDADE),
    v_origem);

  -- Automacao: alerta critico vira evento no log de processamento
  -- (PRC_LOG e autonoma, entao o registro fica mesmo se houver rollback).
  IF INSERTING AND :NEW.NV_SEVERIDADE = 'CRITICO' THEN
    PKG_SHAS_UTIL.PRC_LOG('TRG_SHAS_ALERTA_AUDITORIA', PKG_SHAS_UTIL.c_aviso,
                          'Alerta CRITICO ' || :NEW.ID_ALERTA || ' para o usuario ' || :NEW.ID_USUARIO
                          || ': ' || :NEW.DS_MENSAGEM);
  END IF;
END TRG_SHAS_ALERTA_AUDITORIA;
/

-- ------------------------------------------------------------
-- TRG_SHAS_ALERTA_STATUS (BEFORE UPDATE OF ST_ALERTA ... FOR EACH ROW)
-- Quem resolve o alerta (API, painel, procedure ou SQL manual) so
-- precisa mudar o status; a data de resolucao e mantida pelo banco.
-- ------------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_SHAS_ALERTA_STATUS
BEFORE UPDATE OF ST_ALERTA ON T_SHAS_ALERTA
FOR EACH ROW
BEGIN
  IF :NEW.ST_ALERTA = 'RESOLVIDO' AND :OLD.ST_ALERTA = 'ABERTO' THEN
    :NEW.DT_RESOLUCAO := NVL(:NEW.DT_RESOLUCAO, SYSTIMESTAMP);
  ELSIF :NEW.ST_ALERTA = 'ABERTO' THEN
    :NEW.DT_RESOLUCAO := NULL;   -- alerta reaberto
  END IF;
END TRG_SHAS_ALERTA_STATUS;
/

-- ------------------------------------------------------------
-- TRG_SHAS_MEDICAO_DISPOSITIVO (BEFORE INSERT ... FOR EACH ROW WHEN)
-- A clausula WHEN faz a trigger rodar apenas para leituras com aparelho.
-- Consulta/atualiza T_SHAS_DISPOSITIVO (outra tabela: sem mutating table).
-- ------------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_SHAS_MEDICAO_DISPOSITIVO
BEFORE INSERT ON T_SHAS_MEDICAO
FOR EACH ROW
WHEN (NEW.ID_DISPOSITIVO IS NOT NULL)
DECLARE
  v_dono  T_SHAS_DISPOSITIVO.ID_USUARIO%TYPE;
  v_ativo T_SHAS_DISPOSITIVO.ST_ATIVO%TYPE;
BEGIN
  BEGIN
    SELECT ID_USUARIO, ST_ATIVO INTO v_dono, v_ativo
      FROM T_SHAS_DISPOSITIVO
     WHERE ID_DISPOSITIVO = :NEW.ID_DISPOSITIVO;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      RAISE_APPLICATION_ERROR(-20032, 'Dispositivo ' || :NEW.ID_DISPOSITIVO || ' nao encontrado.');
  END;

  IF v_dono <> :NEW.ID_USUARIO THEN
    RAISE_APPLICATION_ERROR(-20030, 'Dispositivo ' || :NEW.ID_DISPOSITIVO || ' pertence a outro paciente.');
  ELSIF v_ativo = 0 THEN
    RAISE_APPLICATION_ERROR(-20031, 'Dispositivo ' || :NEW.ID_DISPOSITIVO || ' esta inativo.');
  END IF;

  :NEW.TP_ORIGEM := 'SENSOR';

  UPDATE T_SHAS_DISPOSITIVO
     SET DT_ULTIMA_SYNC = SYSTIMESTAMP
   WHERE ID_DISPOSITIVO = :NEW.ID_DISPOSITIVO;
END TRG_SHAS_MEDICAO_DISPOSITIVO;
/
