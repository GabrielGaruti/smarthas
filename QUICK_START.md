# 🚀 Smart HAS - Instruções de Importação

## Como abrir o projeto no Android Studio

### 📋 Checklist Pré-Importação

- [ ] Android Studio Flamingo (2023.1) ou superior instalado
- [ ] SDK 34 instalado (Settings → SDK Manager)
- [ ] Kotlin plugin ativado
- [ ] Gradle 8.1.0 disponível

---

## ✅ Passo-a-passo (5 minutos)

### 1. Fechar projeto atual
```
File → Close Project
```

### 2. Importar novo projeto
```
File → Open
```

### 3. Navegar até a pasta
```
C:\Users\gabri\OneDrive\Área de Trabalho\smarthas-android
```

### 4. Selecionar e abrir
- Clique na pasta `smarthas-android`
- Botão **OK**

### 5. Aguardar Gradle Sync
- Pode levar 2-5 minutos na primeira vez
- Status na barra inferior

### 6. Build do Projeto
```
Build → Make Project
```

### 7. Conectar emulador ou dispositivo
- Emulador: AVD Manager → Create/Start emulator
- Dispositivo: Conectar via USB + ativar Debug Mode

### 8. Executar app
```
Run → Run 'app'  (ou Shift+F10)
```

---

## 🎯 Telas Esperadas (em ordem)

1. **Splash Screen** (2 seg)
   - Logo + app name
   - Versão 1.0 - 2025

2. **Login Screen**
   - Email: `test@email.com`
   - Senha: `123456`
   - Links: Cadastro / Esqueceu?

3. **Home Screen** (se logado)
   - Greeting personalizado
   - Última medição em card
   - Status com badge
   - Botão "Nova Medição"

4. **Nova Medição**
   - Sistólica: 120
   - Diastólica: 80
   - Notas: opcional
   - Botão Salvar

5. **Histórico**
   - Lista de medições
   - Filtro por status
   - Cards coloridos

6. **Créditos**
   - Gabriel Garuti Paiva Cracco
   - RM554866
   - Versão 1.0 - 2025

---

## ⚠️ Possíveis Problemas & Soluções

### ❌ "Gradle sync failed"
```
✓ File → Sync Now
✓ Ou: File → Settings → Build → Gradle → reset cache
✓ Ou: feche e abra novamente
```

### ❌ "SDK 34 not found"
```
✓ Tools → SDK Manager
✓ Procure "Android 14" 
✓ Instale "Android SDK"
✓ Aguarde conclusão
```

### ❌ "Kotlin not recognized"
```
✓ Kotlin plugin vem pré-instalado
✓ Restart Android Studio
✓ File → Invalidate Caches → Restart
```

### ❌ "MainActivity cannot be resolved"
```
✓ Build → Clean Project
✓ Build → Make Project
✓ Ctrl+S para salvar
```

### ❌ "Emulator not starting"
```
✓ AVD Manager → Select device → Edit
✓ Aumentar RAM (mínimo 2GB recomendado)
✓ Desabilitar "Use Host GPU"
```

---

## 🔧 Configurações Backend

Se quer testar a API, certifique-se de:

### 1. FastAPI rodando
```bash
cd new-raffle-backend-api
python -m uvicorn main:app --reload --port 8000
```

### 2. Endpoints disponíveis
```
http://localhost:8000/auth/register
http://localhost:8000/auth/login
http://localhost:8000/measurements
```

### 3. Se em emulador
- Use `10.0.2.2` em vez de `localhost`
- Ou configure o IP real da máquina

---

## 📱 Testando no Emulador

### Criar medição de teste
1. Abra app
2. Faça login
3. Clique "+ Nova Medição"
4. Sistólica: **130**
5. Diastólica: **85**
6. Salvar
7. Badge deve ser **AMARELO (Elevada)**

### Testar classificação
```
NORMAL (Verde):     < 120 / < 80
ELEVADA (Amarelo):  120-139 / 80-89
HIPERTENSÃO (Vermelho): ≥ 140 / ≥ 90
```

---

## 🌙 Testar Modo Escuro

### Ativar:
```
Settings → System → Display → Dark theme
```

### Desativar:
```
Settings → System → Display → Light theme
```

Colors automaticamente adaptam via `values-night/colors.xml`

---

## 📦 Estrutura de Pastas Criada

```
smarthas-android/
├── app/
│   ├── src/main/
│   │   ├── java/com/smarthas/
│   │   │   ├── data/
│   │   │   │   ├── api/
│   │   │   │   ├── database/
│   │   │   │   ├── preferences/
│   │   │   │   └── repository/
│   │   │   ├── presentation/
│   │   │   │   └── viewmodel/
│   │   │   ├── ui/
│   │   │   │   ├── activities/
│   │   │   │   └── fragments/
│   │   │   └── MainActivity.kt
│   │   ├── res/
│   │   │   ├── layout/ (9 arquivos XML)
│   │   │   ├── navigation/
│   │   │   ├── menu/
│   │   │   ├── drawable/ (4 ícones)
│   │   │   ├── values/ (colors, strings, themes, dimens)
│   │   │   ├── values-night/ (colors escuro)
│   │   │   └── color/
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── local.properties
```

---

## ✨ Dicas Profissionais

1. **Usar AVD com hardware acceleration**
   - Settings → Emulator → Enable GPU
   - Muito mais rápido

2. **Debug mode durante desenvolvimento**
   - Retrofit já tem logging ativado
   - Veja logs em: View → Tool Windows → Logcat

3. **Hot reload com Compose**
   - Não aplicável (usando XML)
   - Mas Ctrl+S recompila rápido

4. **Database explorer**
   - View → Tool Windows → Database
   - Veja dados do Room em tempo real

5. **Aumentar heap para gradle
   - Settings → Build → Gradle → Gradle VM options
   - `-Xmx4096m` para 4GB

---

## 📞 Troubleshooting Final

| Erro | Solução |
|------|---------|
| Port 8000 em uso | `netstat -ano \| findstr :8000` |
| App não conecta API | Verificar `http://localhost:8000/auth/login` |
| Emulator lento | Aumentar cores/RAM no AVD |
| Sync infinito | Invalidate Caches → Restart |

---

## 🎓 Próximas Melhorias

- [ ] Adicionar Firebase Analytics
- [ ] Implementar Workmanager para sync background
- [ ] Adicionar unit tests
- [ ] Configurar CI/CD com GitHub Actions
- [ ] Publicar no Google Play Store

---

## 📚 Documentação do Projeto

Todos os arquivos estão em:
- **PROJECT_CONTEXT.md** - Contexto geral do projeto
- **IMPLEMENTATION_COMPLETE.md** - Resumo do que foi criado
- **QUICK_START.md** - Este arquivo

---

**Projeto Pronto para Usar! 🚀**

Qualquer dúvida, consulte os arquivos markdown ou abra uma issue.

Desenvolvido por: **Gabriel Garuti Paiva Cracco (RM554866)**  
FIAP - Fase 3 | 2025
