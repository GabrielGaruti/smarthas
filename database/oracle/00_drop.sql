-- ============================================================
-- Smart HAS - Fase 6 | 00_drop.sql
-- Remove os objetos do projeto (use para reexecutar do zero).
-- Os erros "nao existe" sao ignorados.
-- ============================================================
BEGIN
  FOR o IN (
    SELECT object_name, object_type FROM user_objects
     WHERE object_name LIKE 'PRC\_SHAS\_%' ESCAPE '\'
        OR object_name LIKE 'FN\_SHAS\_%'  ESCAPE '\'
  ) LOOP
    EXECUTE IMMEDIATE 'DROP ' || o.object_type || ' ' || o.object_name;
  END LOOP;

  FOR t IN (
    SELECT table_name FROM user_tables WHERE table_name IN (
      'T_SHAS_LOG_PROCESSAMENTO','T_SHAS_RELATORIO_RESUMO','T_SHAS_ALERTA',
      'T_SHAS_MEDICAO','T_SHAS_DISPOSITIVO','T_SHAS_UNIDADE_SAUDE','T_SHAS_USUARIO')
  ) LOOP
    EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS PURGE';
  END LOOP;
END;
/
