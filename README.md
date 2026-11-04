# Smart HAS — Entrega da Fase 7 (PL/SQL consolidado + CI/CD)

[![Smart HAS CI/CD](https://github.com/GabrielGaruti/smarthas/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/GabrielGaruti/smarthas/actions/workflows/ci-cd.yml)

Monitoramento de **Hipertensão Arterial Sistêmica (HAS)**: registro de medições de pressão,
classificação automática (Normal / Elevada / Hipertensão) e apoio à decisão.

Esta entrega é composta por **uma API central em Java/Spring Boot** consumida por **dois clientes**:
um **app mobile em React Native** e um **painel web em Angular**.

Na **Fase 6** o sistema ganhou uma camada de persistência **Oracle** com **procedures e functions PL/SQL**
integradas ao back-end Java (REST → Java → JDBC → Oracle), módulo de **alertas clínicos**, **relatórios**
consolidados e leituras de **sensores IoT**.

Na **Fase 7** a camada Oracle foi consolidada e a entrega foi automatizada:
- **Packages PL/SQL** `PKG_SHAS_UTIL`, `PKG_SHAS_INDICADORES`, `PKG_SHAS_ALERTAS` e `PKG_SHAS_RELATORIOS`;
- **Triggers** de auditoria (`TRG_SHAS_ALERTA_AUDITORIA`) e automação (`TRG_SHAS_ALERTA_STATUS`, `TRG_SHAS_MEDICAO_DISPOSITIVO`);
- **SQL dinâmico** na busca com filtros combináveis (`PRC_CONSULTAR_MEDICOES`) e no `GROUP BY` por dimensão (`FN_ESTATISTICA_POR_DIMENSAO`);
- **Pipeline CI/CD no GitHub Actions** (`.github/workflows/ci-cd.yml`): build e testes automatizados, Oracle efêmero
  com instalação do schema e testes de integração, imagem Docker no GHCR, deploy em staging com smoke test,
  logs e relatórios de cada execução.

```
smarthas/
├── .github/workflows/    → Fase 7: pipeline CI/CD (GitHub Actions)
├── ci/                   → Fase 7: smoke test pós-deploy e resumo dos testes do pipeline
├── database/             → scripts Oracle (DDL, packages, triggers, carga, testes) + DER
├── backend-springboot/   → API REST em Java + Spring Boot (JWT, JPA, Swagger, JDBC/PL-SQL) + Dockerfile
├── web-angular/          → Parte 3: painel administrativo em Angular
├── mobile-react-native/  → Parte 1: app mobile migrado de Flutter para React Native (Expo)
├── docs/                 → documentação em PDF
└── slides/              → apresentação em PDF
```

## Ordem de execução

### 0) Banco Oracle
Execute os scripts de `database/oracle` na ordem 01 → 08, ou tudo de uma vez com `run_all.sql`
(detalhes em `database/README.md`). Em um banco que já tem os dados da Fase 6, use `deploy_plsql.sql`.

### 1) Back-end (precisa subir primeiro) — porta 8080
Requisitos: Java 17+ e Maven (ou uma IDE como IntelliJ/VS Code).

**Com Oracle (perfil `oracle`, usa os packages PL/SQL):**
```bash
cd backend-springboot
# Windows (PowerShell):  $env:ORACLE_USER="rm554866"; $env:ORACLE_PASSWORD="sua_senha"
# Linux/Mac:             export ORACLE_USER=rm554866 ORACLE_PASSWORD=sua_senha
mvn spring-boot:run -Dspring-boot.run.profiles=oracle
```
Servidor padrão: `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL` (altere com a variável `ORACLE_URL`).

**Sem Oracle (perfil padrão, H2 + as mesmas regras em Java):**
```bash
cd backend-springboot
mvn spring-boot:run
```
- Página de visão geral (Thymeleaf): http://localhost:8080/
- Documentação Swagger: http://localhost:8080/swagger-ui.html
- Console do banco H2: http://localhost:8080/h2-console

**Usuários de demonstração** (criados automaticamente):
- `admin@smarthas.com` / `admin123` — perfil ADMIN
- `paciente@smarthas.com` / `123456` — perfil USER (os demais pacientes da carga também usam `123456`)

**Testes automatizados:** `mvn verify` roda os testes (JUnit 5, MockMvc, H2 em memória) e gera a cobertura
em `target/site/jacoco/index.html`. Os testes de integração com Oracle (`@Tag("oracle")`) rodam no pipeline.

**Docker:** `docker build -t smarthas-api backend-springboot` e `docker run -p 8080:8080 smarthas-api`.

**Endpoints ligados ao Oracle** (todos no Swagger):
- `POST /measurements` → grava a leitura e o evento de back-end chama `PKG_SHAS_ALERTAS.PRC_REGISTRAR_ALERTA`
- `GET /alerts`, `PATCH /alerts/{id}/resolve` → alertas do paciente
- `GET /recommendations` → inclui `PKG_SHAS_INDICADORES.FN_TAXA_CONTROLE` e `FN_RESUMO_PACIENTE`
- `POST /admin/reports/summary?days=30` → `PKG_SHAS_RELATORIOS.PRC_GERAR_RELATORIO_RESUMO` (ADMIN)
- `GET /admin/patients/indicators`, `GET /admin/reports`, `GET /admin/alerts` (ADMIN)
- **Fase 7:** `GET /admin/measurements/search` (SQL dinâmico), `GET /admin/measurements/stats?dimension=`
  (GROUP BY dinâmico), `POST /admin/patients/{id}/alerts/resolve` (procedure + trigger),
  `GET /admin/alerts/{id}/audit` (trilha gravada pela trigger)

## Pipeline CI/CD (GitHub Actions)

| Job | Quando | O que faz |
|---|---|---|
| `backend` | todo push/PR | `mvn verify`: build, testes, JaCoCo; publica relatórios e o `.jar` |
| `web` | todo push/PR | `ng build --configuration production` do painel Angular |
| `oracle-integration` | todo push/PR | Oracle Database Free em container → `run_all.sql` (schema, packages, triggers, carga, testes SQL) → testes de integração Java/JDBC/PL-SQL |
| `deploy` | push na `main` ou disparo manual | imagem Docker no GHCR → banco de staging migrado → API em container com perfil `oracle` → smoke test; deploy em produção opcional via secrets `ORACLE_PROD_URL/USER/PASSWORD` e `DEPLOY_HOOK_URL` |

Cada execução gera um resumo na aba **Summary** e artefatos com logs (build, instalação do banco,
testes, API) e relatórios (Surefire, JaCoCo, smoke test).

### 2) Painel web Angular — porta 4200
Requisitos: Node.js 18+ e Angular CLI (`npm i -g @angular/cli`).
```bash
cd web-angular
npm install
npm start
```
Acesse http://localhost:4200

### 3) App mobile React Native (Expo)
Requisitos: Node.js 18+ e o app **Expo Go** no celular (ou um emulador).
```bash
cd mobile-react-native
npm install
npx expo start
```
> Ajuste a constante `API_URL` em `src/api/client.js` conforme o ambiente
> (emulador Android: `10.0.2.2`; celular físico: IP da sua máquina na rede).

## Integrantes do grupo
- Gabriel Garuti Paiva Cracco — RM 554866
- Vitor Cremonezi Morija — RM 550998


