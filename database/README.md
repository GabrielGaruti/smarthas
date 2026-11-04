# Smart HAS — Camada Oracle (Fase 7)

## Ordem de execução (SQL Developer → abrir o arquivo → **F5 / Executar Script**)

| # | Arquivo | O que faz |
|---|---------|-----------|
| 0 | `oracle/00_drop.sql` | (opcional) apaga tudo para reexecutar do zero |
| 1 | `oracle/01_ddl_tabelas.sql` | cria as 8 tabelas (inclui `T_SHAS_AUDITORIA_ALERTA`), constraints e índices |
| 2 | `oracle/02_pkg_shas_util.sql` | `PKG_SHAS_UTIL`: log, classificação, formatação |
| 3 | `oracle/03_pkg_shas_indicadores.sql` | `PKG_SHAS_INDICADORES`: indicadores e **SQL dinâmico** |
| 4 | `oracle/04_pkg_shas_alertas.sql` | `PKG_SHAS_ALERTAS`: regra de alerta, sensores IoT, resolução |
| 5 | `oracle/05_pkg_shas_relatorios.sql` | `PKG_SHAS_RELATORIOS`: relatório consolidado e nível de risco |
| 6 | `oracle/06_triggers.sql` | triggers de auditoria e automação |
| 7 | `oracle/07_carga_dados.sql` | carga simulada (usuários, unidades, dispositivos, leituras, alertas) |
| 8 | `oracle/08_testes_e_consultas.sql` | demonstra packages, SQL dinâmico, triggers e cenários de erro |
| — | `oracle/99_validar_objetos.sql` | falha se algum package/trigger estiver `INVALID` (usado pelo pipeline) |
| — | `oracle/run_all.sql` | instalação completa do zero (00 → 08 + validação), usada pelo CI/CD |
| — | `oracle/deploy_plsql.sql` | deploy incremental sem apagar dados (banco que já tem a Fase 6) |

> Ative a saída do `DBMS_OUTPUT` (menu *Exibir → Saída DBMS*) para ver as mensagens.
> No SQL*Plus: `sqlplus usuario/senha@host:1521/servico @run_all.sql`.

## Packages

| Package | Membros públicos | Finalidade |
|---------|------------------|-----------|
| `PKG_SHAS_UTIL` | `PRC_LOG`, `FN_CLASSIFICAR`, `FN_FORMATAR_MEDICAO`, constantes da meta (140/90) | apoio usado por todos os outros objetos |
| `PKG_SHAS_INDICADORES` | `FN_TAXA_CONTROLE`, `FN_RESUMO_PACIENTE`, `PRC_CONSULTAR_MEDICOES`, `FN_ESTATISTICA_POR_DIMENSAO` | indicadores do app/painel e consultas dinâmicas |
| `PKG_SHAS_ALERTAS` | `PRC_REGISTRAR_ALERTA`, `PRC_SIMULAR_LEITURAS_SENSOR`, `PRC_RESOLVER_ALERTAS_PACIENTE` | regra clínica de alertas — **acionada pelo back-end Java** |
| `PKG_SHAS_RELATORIOS` | `PRC_GERAR_RELATORIO_RESUMO`, `FN_NIVEL_RISCO` | rotina batch acionada pelo painel admin |

## Triggers

| Trigger | Evento | Tipo | Finalidade |
|---------|--------|------|-----------|
| `TRG_SHAS_ALERTA_AUDITORIA` | AFTER INSERT/UPDATE/DELETE em `T_SHAS_ALERTA` | auditoria | histórico em `T_SHAS_AUDITORIA_ALERTA` + log de alerta CRÍTICO |
| `TRG_SHAS_ALERTA_STATUS` | BEFORE UPDATE OF `ST_ALERTA` | automação | preenche/limpa `DT_RESOLUCAO` |
| `TRG_SHAS_MEDICAO_DISPOSITIVO` | BEFORE INSERT em `T_SHAS_MEDICAO` (WHEN com aparelho) | automação/integridade | valida aparelho (ativo e do paciente), marca `SENSOR`, atualiza `DT_ULTIMA_SYNC` |

## SQL dinâmico

- `PRC_CONSULTAR_MEDICOES`: monta o `WHERE` só com os filtros informados (paciente, classificação, origem,
  período, sistólica mínima), `ORDER BY` por whitelist e `FETCH FIRST :limite`. Usa `EXECUTE IMMEDIATE ... INTO ... USING`
  para o total e `OPEN cursor FOR ... USING` para as linhas (REF CURSOR lido pelo Java).
- `FN_ESTATISTICA_POR_DIMENSAO`: `GROUP BY` escolhido em tempo real (`CLASSIFICACAO`, `ORIGEM`, `PACIENTE`, `PERIODO_DIA`).
- Valores sempre como bind variables; nomes de colunas só de listas fechadas — sem SQL injection.

## Códigos de erro

| Código | Origem | Significado |
|--------|--------|-------------|
| -20001 | indicadores / alertas | usuário não encontrado |
| -20002 | indicadores | período inválido |
| -20003 / -20004 | `PKG_SHAS_UTIL` | parâmetros nulos / medição não encontrada |
| -20010 | `PRC_REGISTRAR_ALERTA` | medição não encontrada |
| -20011 | `PRC_GERAR_RELATORIO_RESUMO` | período fora de 1 a 365 dias |
| -20012 / -20013 | `PRC_SIMULAR_LEITURAS_SENSOR` | dispositivo inexistente/inativo / quantidade inválida |
| -20020 / -20021 / -20022 | SQL dinâmico | filtro inválido / ordenação ou dimensão inválida / limite inválido |
| -20030 / -20031 / -20032 | `TRG_SHAS_MEDICAO_DISPOSITIVO` | aparelho de outro paciente / inativo / inexistente |
| -20099 | `99_validar_objetos.sql` | deploy com objetos inválidos |

## DER
`der/smarthas_der.png` (fonte Graphviz em `der/smarthas_der.dot`).
