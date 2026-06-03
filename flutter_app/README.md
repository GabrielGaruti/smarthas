# Smart HAS — Flutter (Fase 4 FIAP)

Versão Flutter do Smart HAS, adicionada ao repositório existente sem substituir o app Android ou o backend Python.

---

## 1. O QUE FOI ENCONTRADO NO PROJETO ATUAL

### Backend (Python/FastAPI) — `backend/main.py`
| Componente | Detalhe |
|---|---|
| Framework | FastAPI + SQLModel + SQLite |
| Autenticação | JWT (python-jose + passlib/bcrypt) |
| `POST /auth/register` | Cadastro de usuário |
| `POST /auth/login` | Login, retorna `{ token, user }` |
| `GET /measurements` | Lista medições do usuário autenticado |
| `POST /measurements` | Cria nova medição |
| `GET /health` | Health check |

### App Android (Kotlin/MVVM) — `app/`
| Componente | Localização |
|---|---|
| `SmartHasApi.kt` | Interface Retrofit — endpoints espelhados acima |
| `ApiModels.kt` | DTOs: LoginRequest/Response, MeasurementRequest/Response |
| `AuthRepository.kt` | Login, logout, token, isLoggedIn |
| `MeasurementRepository.kt` | CRUD medições + fallback local (Room) |
| `AuthViewModel.kt` | States: LoginState, RegisterState |
| `MeasurementViewModel.kt` | States + classificação de pressão |
| `TokenManager.kt` | SharedPreferences: jwt_token, user_email, user_name |
| `AppDatabase.kt` | Room: measurements + users |
| `MeasurementDao.kt` | Queries Room |
| `LoginFragment.kt` | Tela login |
| `RegisterFragment.kt` | Tela cadastro |
| `HomeFragment.kt` | Tela home com última medição |
| `HistoryFragment.kt` | Histórico com filtro spinner |
| `MainActivity.kt` | BottomNavigationView + NavController |

---

## 2. O QUE JÁ EXISTIA (preservado integralmente)

- ✅ `backend/main.py` — nenhuma alteração
- ✅ `backend/requirements.txt` — nenhuma alteração
- ✅ `app/` inteiro — nenhuma alteração
- ✅ `build.gradle.kts`, `settings.gradle.kts` — nenhuma alteração
- ✅ Toda a lógica de negócio (classificação de pressão, fallback offline, JWT)

---

## 3. O QUE FOI CRIADO (somente Flutter)

Toda a estrutura Flutter foi adicionada em `flutter_app/` — **pasta nova, isolada**.

```
flutter_app/
├── pubspec.yaml                          # Dependências Flutter
├── android/
│   └── app/src/main/
│       └── AndroidManifest.xml          # Permissões + Maps API Key + Firebase
└── lib/
    ├── main.dart                         # Entry point + MultiProvider + navegação
    ├── models/
    │   ├── measurement.dart              # Espelha Measurement.kt + ApiModels.kt
    │   └── user.dart                     # Espelha ApiModels.kt (UserResponse)
    ├── services/
    │   ├── api_service.dart              # Consome mesma REST API do Android
    │   ├── weather_service.dart          # Segunda API externa (OpenWeatherMap)
    │   └── notification_service.dart    # Firebase Messaging
    ├── repositories/
    │   ├── auth_repository.dart          # Espelha AuthRepository.kt
    │   └── measurement_repository.dart  # Espelha MeasurementRepository.kt
    ├── providers/
    │   ├── auth_provider.dart            # Espelha AuthViewModel.kt
    │   ├── measurement_provider.dart     # Espelha MeasurementViewModel.kt
    │   └── map_provider.dart             # Novo — estado do mapa
    ├── utils/
    │   └── app_theme.dart               # Tema visual alinhado ao Android
    ├── widgets/
    │   ├── pressure_card.dart           # Card reutilizável de medição
    │   ├── loading_button.dart          # Botão reutilizável com loading
    │   └── weather_widget.dart          # Widget de clima (segunda API)
    └── screens/
        ├── login_screen.dart            # Espelha LoginFragment.kt
        ├── register_screen.dart         # Espelha RegisterFragment.kt
        ├── home_screen.dart             # Espelha HomeFragment.kt
        ├── history_screen.dart          # Espelha HistoryFragment.kt
        ├── add_measurement_screen.dart  # Tela de nova medição
        └── map_screen.dart              # Google Maps (novo — Fase 4)
```

---

## 4. O QUE FOI ALTERADO

**Nada foi alterado** nos arquivos existentes.
Toda a Fase 4 foi implementada como adição de uma nova pasta `flutter_app/`.

---

## 5. ÁRVORE FINAL DO PROJETO

```
smarthas/
├── .gradle/
├── .idea/
├── app/                                  ← Android Kotlin (ORIGINAL, intocado)
│   └── src/main/java/com/smarthas/
│       ├── data/
│       │   ├── api/          (SmartHasApi.kt, ApiModels.kt)
│       │   ├── database/     (AppDatabase, Dao, Measurement, User)
│       │   ├── preferences/  (TokenManager.kt)
│       │   └── repository/   (AuthRepository.kt, MeasurementRepository.kt)
│       ├── presentation/viewmodel/ (AuthViewModel.kt, MeasurementViewModel.kt)
│       └── ui/
│           ├── activities/   (MainActivity.kt)
│           └── fragments/    (Login, Register, Home, History, ...)
├── backend/                              ← Python FastAPI (ORIGINAL, intocado)
│   ├── main.py
│   └── requirements.txt
├── flutter_app/                          ← ★ NOVO — Flutter Fase 4
│   ├── pubspec.yaml
│   ├── android/app/src/main/AndroidManifest.xml
│   └── lib/
│       ├── main.dart
│       ├── models/           (measurement.dart, user.dart)
│       ├── services/         (api_service, weather_service, notification_service)
│       ├── repositories/     (auth_repository, measurement_repository)
│       ├── providers/        (auth_provider, measurement_provider, map_provider)
│       ├── utils/            (app_theme.dart)
│       ├── widgets/          (pressure_card, loading_button, weather_widget)
│       └── screens/          (login, register, home, history, add_measurement, map)
├── gradle/wrapper/
├── build.gradle.kts
├── gradle.properties
├── gradlew.bat
├── local.properties
└── settings.gradle.kts
```

---

## 6. ARQUIVOS A COMMITAR

Apenas os arquivos **novos** dentro de `flutter_app/`:

```bash
git add flutter_app/
git commit -m "feat: adiciona versão Flutter - Fase 4 FIAP

- Flutter app com Provider (AuthProvider, MeasurementProvider, MapProvider)
- Consome API SmartHAS existente (login, medições, histórico)
- Google Maps com marcadores: usuário, hospital, sensor IoT, wearable
- Firebase Core + Firebase Messaging com exemplo de push notification
- Segunda API externa: OpenWeatherMap (dados climáticos na HomeScreen)
- Repository Pattern espelhando AuthRepository.kt e MeasurementRepository.kt
- Widgets reutilizáveis: PressureCard, LoadingButton, WeatherWidget
- Telas: Login, Register, Home, History, AddMeasurement, Map
- Sem alterações no app Android ou backend Python existentes"
```

---

## 7. CONFIGURAÇÕES NECESSÁRIAS ANTES DE RODAR

### Google Maps
1. Obtenha uma API key em: https://console.cloud.google.com
2. Substitua `YOUR_GOOGLE_MAPS_API_KEY` em `android/app/src/main/AndroidManifest.xml`

### Firebase
1. Crie projeto em: https://console.firebase.google.com
2. Adicione app Android com package `com.smarthas.flutter` (ou o seu)
3. Baixe `google-services.json` → coloque em `flutter_app/android/app/`
4. Adicione ao `flutter_app/android/app/build.gradle`:
   ```groovy
   apply plugin: 'com.google.gms.google-services'
   ```

### OpenWeatherMap (opcional para produção)
1. Crie conta em: https://openweathermap.org/api
2. Substitua `YOUR_OPENWEATHERMAP_API_KEY` em `lib/services/weather_service.dart`
3. Sem a chave, o app usa dados simulados automaticamente

### Backend
- Certifique-se que o backend Python está rodando em `http://10.0.2.2:8000` (emulador)
- Para dispositivo físico, substitua o IP em `lib/services/api_service.dart`

### Instalar dependências e rodar
```bash
cd flutter_app
flutter pub get
flutter run
```

---

## 8. MAPEAMENTO DOS REQUISITOS FASE 4

| Requisito | Status | Implementação |
|---|---|---|
| Flutter estrutura mínima | ✅ | `lib/{screens,widgets,models,services,repositories,providers,utils}` |
| Tela Login | ✅ | `screens/login_screen.dart` |
| Tela Home | ✅ | `screens/home_screen.dart` |
| Tela Histórico | ✅ | `screens/history_screen.dart` |
| Tela Cadastro de Medição | ✅ | `screens/add_measurement_screen.dart` |
| Tela Mapa | ✅ | `screens/map_screen.dart` |
| Provider — AuthProvider | ✅ | `providers/auth_provider.dart` |
| Provider — MeasurementProvider | ✅ | `providers/measurement_provider.dart` |
| Provider — MapProvider | ✅ | `providers/map_provider.dart` |
| Google Maps | ✅ | `map_screen.dart` + `MapProvider` |
| Marcadores: usuário, hospital, sensor, IoT | ✅ | `providers/map_provider.dart` |
| firebase_core | ✅ | `pubspec.yaml` + `main.dart` |
| firebase_messaging | ✅ | `services/notification_service.dart` |
| Push notification (título/corpo corretos) | ✅ | "Smart HAS" / "Pressão arterial acima do recomendado." |
| Consumir API SmartHAS (login/medições) | ✅ | `services/api_service.dart` |
| Segunda API externa | ✅ | `services/weather_service.dart` (OpenWeatherMap) |
| Repository Pattern | ✅ | `repositories/auth_repository.dart`, `repositories/measurement_repository.dart` |
| Componentização / Widgets reutilizáveis | ✅ | `widgets/pressure_card.dart`, `loading_button.dart`, `weather_widget.dart` |
| Sem código duplicado | ✅ | Lógica centralizada nos providers e repositories |
