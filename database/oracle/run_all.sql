-- ============================================================
-- Smart HAS - Fase 7 | run_all.sql
-- Instalacao COMPLETA do zero (apaga e recria tudo + carga + testes).
-- Usada pelo pipeline CI/CD no banco Oracle efemero e para
-- reinstalar um ambiente de desenvolvimento:
--   sqlplus usuario/senha@host:1521/servico @run_all.sql
-- ATENCAO: 00_drop.sql apaga os dados. Em banco existente use deploy_plsql.sql.
-- ============================================================
SET ECHO OFF
SET FEEDBACK ON
SET SERVEROUTPUT ON SIZE UNLIMITED
SET DEFINE OFF
WHENEVER SQLERROR EXIT FAILURE ROLLBACK

PROMPT ===== [1/6] Removendo objetos antigos =====
@@00_drop.sql
PROMPT ===== [2/6] Tabelas, constraints e indices =====
@@01_ddl_tabelas.sql
PROMPT ===== [3/6] Packages =====
@@02_pkg_shas_util.sql
@@03_pkg_shas_indicadores.sql
@@04_pkg_shas_alertas.sql
@@05_pkg_shas_relatorios.sql
PROMPT ===== [4/6] Triggers =====
@@06_triggers.sql
PROMPT ===== [5/6] Validacao dos objetos =====
@@99_validar_objetos.sql
PROMPT ===== [6/6] Carga de dados e testes =====
@@07_carga_dados.sql
@@08_testes_e_consultas.sql

PROMPT ===== Instalacao concluida com sucesso =====
EXIT SUCCESS
