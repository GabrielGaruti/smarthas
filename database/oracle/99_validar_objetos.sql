-- ============================================================
-- Smart HAS - Fase 7 | 99_validar_objetos.sql
-- Verificacao pos-deploy usada pelo pipeline: lista erros de
-- compilacao e FALHA se algum package/trigger do projeto estiver
-- INVALID (o SQL*Plus nao aborta sozinho em erro de compilacao).
-- ============================================================
SET SERVEROUTPUT ON;

SELECT name, type, line, position, text
  FROM user_errors
 WHERE name LIKE 'PKG\_SHAS\_%' ESCAPE '\' OR name LIKE 'TRG\_SHAS\_%' ESCAPE '\'
 ORDER BY name, type, sequence;

DECLARE
  v_invalidos  NUMBER;
  v_esperados  CONSTANT NUMBER := 11;   -- 4 specs + 4 bodies + 3 triggers
  v_encontrados NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_invalidos
    FROM user_objects
   WHERE (object_name LIKE 'PKG\_SHAS\_%' ESCAPE '\' OR object_name LIKE 'TRG\_SHAS\_%' ESCAPE '\')
     AND status <> 'VALID';

  SELECT COUNT(*) INTO v_encontrados
    FROM user_objects
   WHERE (object_name LIKE 'PKG\_SHAS\_%' ESCAPE '\' OR object_name LIKE 'TRG\_SHAS\_%' ESCAPE '\')
     AND status = 'VALID';

  DBMS_OUTPUT.PUT_LINE('Objetos PL/SQL validos: ' || v_encontrados || ' de ' || v_esperados
                       || ' | invalidos: ' || v_invalidos);

  IF v_invalidos > 0 OR v_encontrados < v_esperados THEN
    RAISE_APPLICATION_ERROR(-20099, 'Deploy PL/SQL invalido: ' || v_invalidos
                            || ' objeto(s) com erro, ' || v_encontrados || ' valido(s) de ' || v_esperados || '.');
  END IF;
END;
/
