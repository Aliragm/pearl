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

### 1. Banco de Dados (PostgreSQL via Docker Compose)
```bash
docker compose up -d
```
> O container iniciará o PostgreSQL com as migrações versionadas via Flyway na inicialização do backend.

### 2. Backend (Ktor)
```bash
./gradlew :backend:run
```
> A API estará disponível em `http://localhost:8080`.
> Documentação OpenAPI interativa (Swagger UI) disponível em `http://localhost:8080/swagger` e `http://localhost:8080/openapi`.

### 3. Testes Automatizados (Unitários, StatusPages e Arquitetura Konsist)
```bash
./gradlew test
```

### 4. Microsserviço Go
```bash
cd services/ingestor-go
go run main.go
```
