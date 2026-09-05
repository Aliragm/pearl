# Pearl.gg — Monorepo

Repositório unificado da plataforma **Pearl.gg**, desenvolvido como projeto integrado para as disciplinas:
* **DIM0547 — Desenvolvimento de Sistemas Web II**
* **DIM0524 — Desenvolvimento de Sistemas para Dispositivos Móveis**

---

## 🏗️ Estrutura do Monorepo

```text
pearlgg/
├── docs/                        # Documentação da proposta (Sprint 0)
│   └── proposta.md
├── backend/                     # Serviço Principal em Kotlin / Ktor
│   ├── src/
│   └── build.gradle.kts
├── services/                    # Microsserviços
│   └── ingestor-go/             # Microsserviço de Ingestão e Cache em Go
│       ├── go.mod
│       ├── main.go
│       └── main_test.go
├── mobile/                      # Aplicativo Móvel (Kotlin Multiplatform / Compose)
│   ├── src/
│   │   ├── commonMain/          # Regras de negócio e UI compartilhadas (Android/iOS/Desktop)
│   │   ├── androidMain/         # Específico Android
│   │   ├── iosMain/             # Específico iOS
│   │   └── desktopMain/         # Específico Desktop (Hot Reload)
│   └── build.gradle.kts
└── .github/
    └── workflows/
        └── ci.yml               # Pipeline de CI (GitHub Actions)
```

---

## 🚀 Como Executar

### 1. Backend (Ktor)
```bash
./gradlew :backend:run
```

### 2. Testes de Todos os Módulos Gradle
```bash
./gradlew test
```

### 3. Microsserviço Go
```bash
cd services/ingestor-go
go run main.go
```
