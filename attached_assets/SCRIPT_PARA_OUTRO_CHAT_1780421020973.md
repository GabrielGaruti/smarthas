# Smart HAS — Estado atual do projeto + O que falta

## Contexto
Este é um projeto Flutter + FastAPI (Python) chamado **Smart HAS** — app de monitoramento de pressão arterial.
O código foi clonado de https://github.com/GabrielGaruti/smarthas.git e adaptado para rodar no Replit (web).

## Stack
- **Frontend:** Flutter 3.32.0, compilado como web (flutter build web --release)
- **Backend:** Python FastAPI + SQLite (SQLModel), JWT auth, porta 8000
- **Servidor intermediário:** Node.js + Express 5 na porta 8080, que serve os arquivos estáticos do Flutter E faz proxy das chamadas de API para o backend Python

## O que já funciona
- [x] Flutter compilado e rodando no browser
- [x] Tela de login e cadastro (conectadas ao banco de dados real)
- [x] Backend FastAPI com SQLite, endpoints `/auth/register`, `/auth/login`, `/measurements`
- [x] Proxy Express repassando chamadas `/api/auth/*` e `/api/measurements/*` para o FastAPI
- [x] Autenticação JWT funcionando end-to-end
- [x] Telas: Home, Medição, Histórico, Perfil, Mapa (versão sem mapa real)

## O que FALTA implementar

### 1. Google Maps (prioridade alta)
- O pacote `google_maps_flutter` foi **removido** porque não funciona direto na web sem configuração extra.
- Para web, o pacote correto é `google_maps_flutter_web` (já é instalado automaticamente junto com `google_maps_flutter` nas versões recentes).
- **O que precisa ser feito:**
  1. Adicionar `google_maps_flutter: ^2.9.0` de volta no `flutter_app/pubspec.yaml`
  2. Obter uma **Google Maps JavaScript API Key** (console.cloud.google.com) com as APIs ativadas: Maps JavaScript API, Geocoding API
  3. Inserir a chave em `flutter_app/web/index.html`, dentro do `<head>`:
     ```html
     <script src="https://maps.googleapis.com/maps/api/js?key=SUA_CHAVE_AQUI"></script>
     ```
  4. Restaurar o import do `google_maps_flutter` em `flutter_app/lib/providers/map_provider.dart` (trocando `MapLatLng` de volta para `LatLng` do pacote)
  5. Restaurar o widget `GoogleMap(...)` em `flutter_app/lib/screens/map_screen.dart`
  6. Recompilar: `flutter build web --release` dentro da pasta `flutter_app/`

### 2. Firebase Push Notifications (prioridade média)
- O `notification_service.dart` foi simplificado (Firebase removido para o build web funcionar).
- **O que precisa ser feito:**
  1. Criar projeto no Firebase Console (console.firebase.google.com)
  2. Ativar Cloud Messaging
  3. Baixar `google-services.json` (Android) e `GoogleService-Info.plist` (iOS)
  4. Para web: copiar o config do Firebase para `flutter_app/web/index.html` (firebaseConfig object)
  5. Restaurar as dependências no `pubspec.yaml`:
     ```yaml
     firebase_core: ^3.0.0
     firebase_messaging: ^15.0.0
     ```
  6. Reativar o código em `notification_service.dart` e re-adicionar o import em `main.dart`

### 3. Rebuild do Flutter após mudanças
- **Sempre que alterar código Dart**, é necessário recompilar:
  ```bash
  cd flutter_app
  flutter build web --release
  ```
- O output vai para `flutter_app/build/web/` — o Express serve essa pasta automaticamente.

## Arquivos-chave do projeto
```
flutter_app/
  lib/
    main.dart                        # Entry point (Firebase removido)
    services/api_service.dart        # URL base: /api quando web, 10.0.2.2:8000 quando Android
    screens/map_screen.dart          # Tela de mapa (atualmente card-based, sem GoogleMap real)
    providers/map_provider.dart      # Usa MapLatLng customizado (sem google_maps_flutter)
  web/
    index.html                       # Aqui vai a chave da Google Maps API e config do Firebase
  pubspec.yaml                       # Dependências Flutter
  build/web/                         # Build compilado (servido pelo Express)

backend/
  main.py                            # FastAPI: /auth/register, /auth/login, /measurements
  smarthas.db                        # SQLite (gerado automaticamente)

artifacts/api-server/
  src/app.ts                         # Express: serve Flutter + proxy /api/* → Python :8000
```

## Como testar o proxy manualmente
```bash
# Registrar usuário
curl -X POST http://localhost/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Teste","email":"teste@teste.com","password":"123456"}'

# Login
curl -X POST http://localhost/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"teste@teste.com","password":"123456"}'
```
