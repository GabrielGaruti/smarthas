# Smart HAS — Entrega da Fase 6 (Oracle PL/SQL + integração Java)

Monitoramento de **Hipertensão Arterial Sistêmica (HAS)**: registro de medições de pressão,
classificação automática (Normal / Elevada / Hipertensão) e apoio à decisão.

Esta entrega é composta por **uma API central em Java/Spring Boot** consumida por **dois clientes**:
um **app mobile em React Native** e um **painel web em Angular**.

Na **Fase 6** o sistema ganhou uma camada de persistência **Oracle** com **procedures e functions PL/SQL**
integradas ao back-end Java (REST → Java → JDBC → Oracle), módulo de **alertas clínicos**, **relatórios**
consolidados e leituras de **sensores IoT**.

```
smarthas/
├── database/             → Fase 6: scripts Oracle (DDL, functions, procedures, carga, testes) + DER
├── backend-springboot/   → API REST em Java + Spring Boot (JWT, JPA, Swagger, Thymeleaf, JDBC/PL-SQL)
├── web-angular/          → Parte 3: painel administrativo em Angular
├── mobile-react-native/  → Parte 1: app mobile migrado de Flutter para React Native (Expo)
├── docs/                 → documentação em PDF
└── slides/              → apresentação em PDF
```

## Ordem de execução

### 0) Banco Oracle (Fase 6)
Execute os scripts de `database/oracle` na ordem 01 → 05 (detalhes em `database/README.md`).

### 1) Back-end (precisa subir primeiro) — porta 8080
Requisitos: Java 17+ e Maven (ou uma IDE como IntelliJ/VS Code).

**Com Oracle (perfil `oracle`, usa as procedures/functions PL/SQL):**
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

**Novos endpoints da Fase 6** (todos no Swagger):
- `POST /measurements` → grava a leitura e o evento de back-end chama `PRC_SHAS_REGISTRAR_ALERTA`
- `GET /alerts`, `PATCH /alerts/{id}/resolve` → alertas do paciente
- `GET /recommendations` → agora inclui `FN_SHAS_TAXA_CONTROLE` e `FN_SHAS_RESUMO_PACIENTE`
- `POST /admin/reports/summary?days=30` → executa `PRC_SHAS_GERAR_RELATORIO_RESUMO` (ADMIN)
- `GET /admin/patients/indicators`, `GET /admin/reports`, `GET /admin/alerts` (ADMIN)

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


