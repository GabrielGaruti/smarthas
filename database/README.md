# Smart HAS — Camada Oracle (Fase 6)

## Ordem de execução (SQL Developer → abrir o arquivo → **F5 / Executar Script**)

| # | Arquivo | O que faz |
|---|---------|-----------|
| 0 | `oracle/00_drop.sql` | (opcional) apaga tudo para reexecutar do zero |
| 1 | `oracle/01_ddl_tabelas.sql` | cria as 7 tabelas, constraints e índices |
| 2 | `oracle/02_functions.sql` | cria `PRC_SHAS_LOG` e as functions |
| 3 | `oracle/03_procedures.sql` | cria as procedures |
| 4 | `oracle/04_carga_dados.sql` | carga simulada (usuários, unidades, dispositivos, leituras, alertas) |
| 5 | `oracle/05_testes_e_consultas.sql` | demonstra functions em SQL, procedures e cenários de erro |

> Ative a saída do `DBMS_OUTPUT` (menu *Exibir → Saída DBMS*) para ver as mensagens.

## Objetos PL/SQL

| Objeto | Tipo | Finalidade |
|--------|------|-----------|
| `FN_SHAS_TAXA_CONTROLE` | function (indicador) | % de leituras na meta (<140/90) em N dias |
| `FN_SHAS_RESUMO_PACIENTE` | function (dados formatados) | texto-resumo do paciente |
| `FN_SHAS_FORMATAR_MEDICAO` | function (dados formatados) | linha legível de uma leitura |
| `FN_SHAS_CLASSIFICAR` | function (apoio) | Normal / Elevada / Hipertensão (mesma regra do Java) |
| `PRC_SHAS_REGISTRAR_ALERTA` | procedure | avalia uma leitura e grava alerta — **acionada pelo back-end Java** |
| `PRC_SHAS_GERAR_RELATORIO_RESUMO` | procedure | relatório consolidado por paciente — acionada pelo painel admin |
| `PRC_SHAS_SIMULAR_LEITURAS_SENSOR` | procedure | importa leituras simuladas de dispositivos IoT |
| `PRC_SHAS_LOG` | procedure (apoio) | log em transação autônoma |

## DER
`der/smarthas_der.png` (fonte Graphviz em `der/smarthas_der.dot`).
