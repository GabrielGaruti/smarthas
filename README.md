# Smart HAS — Entrega da Fase

Monitoramento de **Hipertensão Arterial Sistêmica (HAS)**: registro de medições de pressão,
classificação automática (Normal / Elevada / Hipertensão) e apoio à decisão.

Esta entrega é composta por **uma API central em Java/Spring Boot** consumida por **dois clientes**:
um **app mobile em React Native** e um **painel web em Angular**.

```
smarthas-entrega/
├── backend-springboot/   → Parte 2: API REST em Java + Spring Boot (JWT, JPA/H2, Swagger, Thymeleaf)
├── web-angular/          → Parte 3: painel administrativo em Angular
├── mobile-react-native/  → Parte 1: app mobile migrado de Flutter para React Native (Expo)
├── docs/                 → documentação em PDF
└── slides/              → apresentação em PDF
```

## Ordem de execução

### 1) Back-end (precisa subir primeiro) — porta 8080
Requisitos: Java 17+ e Maven (ou uma IDE como IntelliJ/VS Code).
```bash
cd backend-springboot
mvn spring-boot:run
```
- Página de visão geral (Thymeleaf): http://localhost:8080/
- Documentação Swagger: http://localhost:8080/swagger-ui.html
- Console do banco H2: http://localhost:8080/h2-console

**Usuários de demonstração** (criados automaticamente):
- `admin@smarthas.com` / `admin123` — perfil ADMIN
- `paciente@smarthas.com` / `123456` — perfil USER

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


