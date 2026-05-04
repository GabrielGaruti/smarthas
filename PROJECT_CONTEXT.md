# Smart HAS — Fase 3 (FIAP) | Contexto do Projeto Kotlin

**Criado em:** 29/04/2026  
**Aluno:** Gabriel Garuti Paiva Cracco (RM554866)  
**Disciplina:** FIAP - Fase 3  

---

## 📱 Resumo Executivo

App mobile nativo em **Kotlin/Android Studio** para monitoramento de pressão arterial. Aplicação de saúde com interface Material Design 3, persistência local (Room Database) e integração com API REST (FastAPI).

---

## 🎯 Requisitos Obrigatórios

✅ **Telas (7 total):**
1. Splash / Boas-vindas
2. Login
3. Cadastro
4. Home / Dashboard
5. Registro de Medição
6. Histórico de Medições
7. Créditos

✅ **Funcionalidades:**
- Navegação entre telas (Bottom Navigation)
- Identidade visual coerente (Material Design 3)
- Listagem com interação (histórico)
- Persistência local (Room Database + SharedPreferences)
- Integração com API (total ou parcial)
- Créditos com nome (Gabriel Garuti Paiva Cracco) e ano (2025)

✅ **Validações:**
- Login/Cadastro funcionando
- Histórico salvando medições
- Classificação por badge (verde/amarelo/vermelho)
- JWT armazenado em SharedPreferences

---

## 🎨 Identidade Visual

| Elemento | Valor |
|----------|-------|
| **Cor Primária** | #2E3A8C (Azul Escuro) |
| **Design System** | Material Design 3 |
| **Badge Normal** | Verde (#4CAF50) |
| **Badge Elevada** | Amarelo (#FFC107) |
| **Badge Hipertensão** | Vermelho (#F44336) |

---

## 🛠️ Stack Técnica

### Frontend
- **Linguagem:** Kotlin
- **IDE:** Android Studio (Gradle)
- **UI:** Material 3 Components
- **Navegação:** Jetpack Navigation
- **Persistência Local:** Room Database + SharedPreferences
- **HTTP Client:** Retrofit + OkHttp
- **JSON:** Gson/kotlinx.serialization

### Backend
- **URL Base:** http://localhost:8000
- **Framework:** FastAPI (Python)
- **Autenticação:** JWT Bearer Token

### Banco de Dados
- **Local:** SQLite (via Room)
- **Remoto:** PostgreSQL (produção)

---

## 📡 Endpoints API

| Método | Endpoint | Body | Resposta |
|--------|----------|------|----------|
| POST | `/auth/register` | `{email, password, name}` | `{token, user_id}` |
| POST | `/auth/login` | `{email, password}` | `{token, user_id}` |
| GET | `/measurements` | - (Header: Authorization) | `[{id, systolic, diastolic, date, time, notes}]` |
| POST | `/measurements` | `{systolic, diastolic, notes?}` | `{id, systolic, diastolic, date, time}` |

---

## 📦 Estrutura Room Database

### Entity: Measurement
```kotlin
@Entity(tableName = "measurements")
data class Measurement(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val systolic: Int,           // e.g., 130
    val diastolic: Int,          // e.g., 85
    val date: String,            // "2025-04-29"
    val time: String,            // "14:30"
    val notes: String? = null,   // opcional
    val timestamp: Long = System.currentTimeMillis()
)
```

### Classification Logic
```
sistólica < 120 E diastólica < 80        → NORMAL (Verde)
sistólica 120-139 OU diastólica 80-89    → ELEVADA (Amarelo)
sistólica ≥ 140 OU diastólica ≥ 90       → HIPERTENSÃO (Vermelho)
```

---

## 📐 Estrutura do Projeto Kotlin

```
smarthas-android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/smarthas/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── di/
│   │   │   │   │   ├── DatabaseModule.kt
│   │   │   │   │   └── NetworkModule.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── api/
│   │   │   │   │   │   ├── ApiService.kt
│   │   │   │   │   │   ├── AuthInterceptor.kt
│   │   │   │   │   │   ├── models/
│   │   │   │   │   │   │   ├── LoginRequest.kt
│   │   │   │   │   │   │   ├── RegisterRequest.kt
│   │   │   │   │   │   │   ├── AuthResponse.kt
│   │   │   │   │   │   │   └── MeasurementResponse.kt
│   │   │   │   │   ├── database/
│   │   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   │   ├── MeasurementDao.kt
│   │   │   │   │   │   └── entities/
│   │   │   │   │   │       └── Measurement.kt
│   │   │   │   │   └── preferences/
│   │   │   │   │       └── TokenManager.kt
│   │   │   │   ├── domain/
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── AuthRepository.kt
│   │   │   │   │   │   └── MeasurementRepository.kt
│   │   │   │   │   └── usecase/
│   │   │   │   │       ├── LoginUseCase.kt
│   │   │   │   │       ├── RegisterUseCase.kt
│   │   │   │   │       ├── SaveMeasurementUseCase.kt
│   │   │   │   │       └── GetMeasurementsUseCase.kt
│   │   │   │   ├── presentation/
│   │   │   │   │   ├── ui/
│   │   │   │   │   │   ├── theme/
│   │   │   │   │   │   │   ├── Color.kt
│   │   │   │   │   │   │   ├── Theme.kt
│   │   │   │   │   │   │   └── Type.kt
│   │   │   │   │   │   └── screens/
│   │   │   │   │   │       ├── splash/
│   │   │   │   │   │       │   ├── SplashFragment.kt
│   │   │   │   │   │       │   └── SplashViewModel.kt
│   │   │   │   │   │       ├── auth/
│   │   │   │   │   │       │   ├── login/
│   │   │   │   │   │       │   │   ├── LoginFragment.kt
│   │   │   │   │   │       │   │   └── LoginViewModel.kt
│   │   │   │   │   │       │   └── register/
│   │   │   │   │   │       │       ├── RegisterFragment.kt
│   │   │   │   │   │       │       └── RegisterViewModel.kt
│   │   │   │   │   │       ├── home/
│   │   │   │   │   │       │   ├── HomeFragment.kt
│   │   │   │   │   │       │   └── HomeViewModel.kt
│   │   │   │   │   │       ├── measurement/
│   │   │   │   │   │       │   ├── NewMeasurementFragment.kt
│   │   │   │   │   │       │   └── NewMeasurementViewModel.kt
│   │   │   │   │   │       ├── history/
│   │   │   │   │   │       │   ├── HistoryFragment.kt
│   │   │   │   │   │       │   └── HistoryViewModel.kt
│   │   │   │   │   │       └── credits/
│   │   │   │   │   │           └── CreditsFragment.kt
│   │   │   │   │   └── viewmodel/
│   │   │   │   │       └── BaseViewModel.kt
│   │   │   │   └── utils/
│   │   │   │       ├── Constants.kt
│   │   │   │       ├── DateTimeUtils.kt
│   │   │   │       └── ValidationUtils.kt
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   ├── fragment_splash.xml
│   │   │   │   │   ├── fragment_login.xml
│   │   │   │   │   ├── fragment_register.xml
│   │   │   │   │   ├── fragment_home.xml
│   │   │   │   │   ├── fragment_new_measurement.xml
│   │   │   │   │   ├── fragment_history.xml
│   │   │   │   │   ├── fragment_credits.xml
│   │   │   │   │   └── item_measurement.xml
│   │   │   │   ├── values/
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   ├── dimens.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   ├── values-night/
│   │   │   │   │   └── colors.xml
│   │   │   │   ├── drawable/
│   │   │   │   ├── menu/
│   │   │   │   │   └── bottom_nav_menu.xml
│   │   │   │   └── navigation/
│   │   │   │       └── nav_graph.xml
│   │   │   └── AndroidManifest.xml
│   │   └── test/ & androidTest/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts (root)
├── settings.gradle.kts
├── gradle.properties
├── local.properties
├── README.md
└── PROJECT_CONTEXT.md (este arquivo)
```

---

## 📋 Dependencies (build.gradle)

```kotlin
// Core
implementation("androidx.core:core-ktx:1.13.1")
implementation("androidx.appcompat:appcompat:1.6.1")

// Material Design 3
implementation("com.google.android.material:material:1.11.0")

// Jetpack Navigation
implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

// Room Database
implementation("androidx.room:room-runtime:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")

// Retrofit + OkHttp
implementation("com.squareup.retrofit2:retrofit:2.10.0")
implementation("com.squareup.retrofit2:converter-gson:2.10.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// SharedPreferences (androidx)
implementation("androidx.security:security-crypto:1.1.0-alpha06")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

// Lifecycle
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")

// Testing
testImplementation("junit:junit:4.13.2")
androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
```

---

## ✅ Checklist de Implementação

### Fase 1: Setup
- [ ] Criar projeto Android Studio com Kotlin
- [ ] Configurar gradle files (root + app)
- [ ] Adicionar dependencies
- [ ] Criar estrutura de pacotes

### Fase 2: Database & Preferences
- [ ] Criar Measurement Entity
- [ ] Criar MeasurementDao
- [ ] Criar AppDatabase
- [ ] Implementar TokenManager (SharedPreferences)

### Fase 3: API Integration
- [ ] Criar models de request/response
- [ ] Implementar ApiService (Retrofit)
- [ ] Implementar AuthInterceptor (JWT)
- [ ] Testar endpoints

### Fase 4: Theme & Resources
- [ ] Configurar colors.xml (#2E3A8C)
- [ ] Implementar Material 3 Theme
- [ ] Criar strings.xml (textos)
- [ ] Configurar dimens.xml

### Fase 5: Telas & Navigation
- [ ] Criar navigation graph
- [ ] Implementar cada Fragment
- [ ] Criar layouts XML
- [ ] Configurar Bottom Navigation

### Fase 6: ViewModels & Repositories
- [ ] Implementar repositories
- [ ] Criar ViewModels
- [ ] Adicionar lógica de negócio

### Fase 7: Testes & Polish
- [ ] Testar navegação
- [ ] Testar persistência
- [ ] Testar chamadas API
- [ ] Ajustar UI/UX

---

## 🚀 Próximos Passos

1. **Abrir pasta** `smarthas-android` no Android Studio
2. **Criar novo projeto** com:
   - Language: Kotlin
   - Minimum API: 24
   - Activity: Empty Activity
3. **Copiar estrutura** de pacotes conforme acima
4. **Começar por:** `build.gradle.kts` (dependencies)
5. **Depois:** Room Database
6. **Depois:** Retrofit + API
7. **Por fim:** Telas e Navegação

---

## 📌 Notas Importantes

- JWT token armazenado em `SharedPreferences` com `EncryptedSharedPreferences`
- Todas as datas em formato ISO: `yyyy-MM-dd`
- Horários em formato 24h: `HH:mm`
- Cache de medições localmente para offline-first
- Material Design 3 colors dinamicamente via `Material3.colorScheme`
- Bottom Navigation fixa com 4 tabs: Home, Nova Medição, Histórico, Créditos
- Splash screen 2-3 segundos antes de ir para Login/Home

---

## 🎥 Referência Wireframes

Veja o arquivo PNG com as 7 telas:
1. Splash (versão 1.0 - 2025)
2. Login (campo email/senha)
3. Cadastro (nome, email, senha, confirmar)
4. Home (última medição, status, botão nova medição)
5. Registro de Medição (sistólica, diastólica, notas opcionais, botão salvar)
6. Histórico (lista com filtro, badges coloridos, interação swipe)
7. Créditos (Gabriel Garuti, RM554866, FIAP 2025)

---

**Última atualização:** 29/04/2026  
**Status:** Pronto para iniciar implementação
