# Smart HAS

App de monitoramento de pressão arterial (hipertensão arterial sistêmica) com Flutter web + FastAPI.

## Run & Operate

- `pnpm --filter @workspace/api-server run dev` — Express na porta 8080 (serve Flutter + proxy para Python)
- Python backend: workflow "Python Backend (FastAPI)" — FastAPI na porta 8000
- Recompilar Flutter após mudanças Dart: `cd flutter_app && flutter build web --release`
- `pnpm run typecheck` — typecheck completo
- `pnpm --filter @workspace/api-spec run codegen` — regenerar hooks/schemas da spec OpenAPI
- `pnpm --filter @workspace/db run push` — push do schema DB (dev only)

## Stack

- pnpm workspaces, Node.js 24, TypeScript 5.9
- **Frontend:** Flutter 3.32.0 compilado como web (flutter build web --release)
- **Backend:** Python FastAPI + SQLite (SQLModel), JWT auth, porta 8000
- **Proxy:** Express 5 na porta 8080 — serve Flutter estático E repassa /api/* para FastAPI
- DB: PostgreSQL + Drizzle ORM (disponível, ainda não usado pelo Smart HAS)
- Validação: Zod (zod/v4), drizzle-zod
- Build: esbuild (CJS bundle)

## Where things live

- `flutter_app/` — código Flutter (Dart). Build output em `flutter_app/build/web/`
- `flutter_app/lib/screens/` — telas: login, register, home, history, map, add_measurement
- `flutter_app/lib/providers/` — gerenciamento de estado (Provider)
- `flutter_app/lib/services/` — api_service.dart (base URL `/api`), weather_service.dart
- `flutter_app/web/index.html` — aqui vai a chave Google Maps API e config Firebase
- `backend/main.py` — FastAPI: /auth/register, /auth/login, /measurements
- `backend/smarthas.db` — SQLite gerado automaticamente
- `artifacts/api-server/src/app.ts` — Express: proxy /api/* → Python :8000, serve Flutter
- `lib/api-spec/openapi.yaml` — spec OpenAPI (para a infra Node; não usada pelo Flutter)

## Architecture decisions

- Express serve tanto o Flutter estático (raiz `/`) quanto faz proxy das chamadas `/api/*` para o FastAPI Python
- Flutter usa URL base `/api` quando rodando na web (detectado por `kIsWeb`)
- SQLite no backend Python para simplicidade (não o PostgreSQL do Drizzle)
- Flutter compilado com `flutter build web --release` — output servido como arquivos estáticos

## Product

- Login e cadastro de usuário com autenticação JWT
- Registro de medições de pressão arterial (sistólica/diastólica, data, hora, notas)
- Histórico de medições com classificação (normal, elevada, hipertensão)
- Tela inicial com última medição e dados de clima
- Tela de mapa (versão sem Google Maps real por enquanto)
- Tela de perfil do usuário

## O que falta implementar

1. ~~**Google Maps** (alta prioridade)~~ ✅ **CONCLUÍDO** — `google_maps_flutter: ^2.9.0` adicionado, API Key configurada em `flutter_app/web/index.html`, widget `GoogleMap` real na tela de mapa com marcadores (hospital, sensores IoT, localização do usuário).
2. ~~**Firebase Push Notifications**~~ ✅ **CONCLUÍDO** — Web Notifications API nativa do browser implementada em `notification_service.dart`. Ao salvar uma medição com pressão acima do normal, o app pede permissão e dispara uma notificação nativa do sistema operacional.

## User preferences

_Populate as you build — explicit user instructions worth remembering across sessions._

## Gotchas

- **Sempre recompilar Flutter após mudar código Dart**: `cd flutter_app && flutter build web --release`
- Express 5 exige `"/{*path}"` para wildcard (não `"*"` como no Express 4)
- O backend Python roda na porta 8000 (workflow "Python Backend (FastAPI)")
- SQLite do Python em `backend/smarthas.db` — NÃO é o PostgreSQL do Drizzle

## Pointers

- See the `pnpm-workspace` skill for workspace structure, TypeScript setup, and package details
