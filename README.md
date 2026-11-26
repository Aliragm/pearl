# Pearl.gg — Monorepo

Repositório unificado da plataforma **Pearl.gg**, desenvolvido como projeto integrado para as disciplinas:
* **DIM0547 — Desenvolvimento de Sistemas Web II**
* **DIM0524 — Desenvolvimento de Sistemas para Dispositivos Móveis**

---

## 🎥 Vídeos de Demonstração

### Sprint 2 — Microsserviço Go, gRPC e Persistência sob Demanda
* 🎬 **Demonstração completa (5 min):** *(link a adicionar após gravação)*

### Sprint 1 (T10)
* 🌐 **Web II (DIM0547 — Backend Kotlin/Ktor, PostgreSQL, Flyway, Clean Architecture, Problem Details e OpenAPI):**  
  👉 [Assistir no YouTube (https://youtu.be/-nqagS4l5iU)](https://youtu.be/-nqagS4l5iU)
* 📱 **Dispositivos Móveis (DIM0524 — Compose Multiplatform, Rotas Tipadas, Deep Link, Adaptatividade e Acessibilidade):**  
  👉 [Assistir no YouTube (https://youtu.be/kVFMg3hMrSY)](https://youtu.be/kVFMg3hMrSY)

---

## 🏗️ Estrutura do Monorepo

```text
pearl/
├── protos/                          # Contratos Protobuf versionados (Buf STANDARD + COMMENTS)
│   ├── buf.yaml
│   ├── buf.gen.yaml
│   └── pearl/catalog/v1/game.proto
├── docs/                            # Documentação e Decisões Arquiteturais
│   ├── proposta.md
│   └── decisoes/
│       ├── 0001-microservico-ingestor-go-grpc.md
│       └── 0002-estrategia-implantacao-containerizacao.md
├── backend/                         # Serviço Principal em Kotlin / Ktor
│   ├── Dockerfile
│   ├── build.gradle.kts
│   └── src/
├── services/                        # Microsserviços
│   └── ingestor-go/                 # Microsserviço gRPC de Ingestão da IGDB em Go
│       ├── Dockerfile
│       ├── go.mod
│       ├── client/                  # Client HTTP da IGDB e testes com httptest
│       ├── domain/                  # Porta GameSearcher e normalização de plataformas
│       ├── proto/                   # Stubs Go/gRPC gerados via Buf
│       └── server/                  # Servidor gRPC (:8081), Health Check e testes com bufconn
├── mobile/                          # Aplicativo Móvel (Kotlin Multiplatform / Compose)
│   ├── src/
│   │   ├── commonMain/              # Regras de negócio e UI compartilhadas (Android/iOS/Desktop)
│   │   ├── androidMain/             # Específico Android
│   │   ├── iosMain/                 # Específico iOS
│   │   └── desktopMain/             # Específico Desktop (Hot Reload)
│   └── build.gradle.kts
├── .github/workflows/ci.yml         # Pipeline de CI (Buf lint, Go, Backend Ktor, Mobile KMP)
├── docker-compose.yml               # Orquestração PostgreSQL + ingestor-go + backend
├── .env.example                     # Modelo de variáveis de ambiente
└── LICENSE                          # Licença MIT
```

---

## 🚀 Como Executar

### 0. Variáveis de Ambiente
Copie `.env.example` para `.env` e preencha as credenciais da Twitch/IGDB (`CLIENT_ID_TWITCH` e `ACCESS_TOKEN`) se for consultar a API real da IGDB:
```bash
cp .env.example .env
```

### 1. Stack Completa via Docker Compose (PostgreSQL + `ingestor-go` + `backend`)
```bash
docker compose up --build -d
```
> O `docker-compose.yml` inicia o `postgres` (`pg_isready`) e o `ingestor-go` (probe gRPC `-healthcheck`) e só sobe o `backend` quando ambos estiverem com status `healthy`.

### 2. Executando Localmente (fora do Docker)

#### Banco de Dados (somente PostgreSQL)
```bash
docker compose up -d postgres
```

#### Microsserviço Go (`ingestor-go` — Servidor gRPC em `:8081`)
```bash
cd services/ingestor-go
go run ./server
```
Para testar uma busca via `grpcurl` (com gRPC Reflection habilitado):
```bash
grpcurl -plaintext -d '{"search_string": "zelda"}' localhost:8081 pearl.catalog.v1.GameService/Search
```

#### Backend (Ktor em `:8080`)
```bash
./gradlew :backend:run
```
> * API: `http://localhost:8080` | Swagger UI: `http://localhost:8080/swagger` | OpenAPI: `http://localhost:8080/openapi`
> * **Busca integrada + fallback:** `GET /games/search?q=zelda` consulta o `ingestor-go` via gRPC com deadline configurado (`INGESTOR_GRPC_DEADLINE_MS`) e faz fallback automático para o PostgreSQL caso o microsserviço esteja indisponível (`X-Catalog-Source: igdb` ou `postgres-fallback`).
> * **Persistência sob demanda:** ao enviar `POST /posts` com os metadados de `game` retornados pela busca (sem `gameId`), o backend persiste o jogo no PostgreSQL de forma idempotente pelo `external_id` antes de salvar a avaliação.

### 3. Contratos Protobuf (Buf) e Testes Automatizados
```bash
# Lint e geração de stubs Go
cd protos && buf lint && buf generate

# Testes do microsserviço Go (normalização, client HTTP, gRPC bufconn e health check)
cd services/ingestor-go && go test -v -race ./...

# Testes do Backend Ktor (Protobuf/gRPC-Kotlin, grpc-inprocess, Konsist, unitários e Testcontainers)
./gradlew :backend:test

# Testes do Mobile KMP
./gradlew :mobile:desktopTest
```

### 4. Aplicativo Móvel — KMP / Compose Multiplatform (DIM0524)

Para executar a versão Desktop (com *Hot Reload* / redimensionamento de janela para testar as 2 classes de largura):
```bash
./gradlew :mobile:run
```

---

## 📐 Decisões Arquiteturais (ADRs)

* [ADR-0001 — Microsserviço `ingestor-go` e Comunicação gRPC com Fallback](docs/decisoes/0001-microservico-ingestor-go-grpc.md)
* [ADR-0002 — Estratégia de Containerização, Orquestração Local e Pipeline de CI](docs/decisoes/0002-estrategia-implantacao-containerizacao.md)

---

## 🔗 Deep Link no App Móvel (T5)

O grafo de navegação tipado (`@Serializable`) declara suporte a *deep links* para abrir diretamente a tela de detalhes de um jogo (`GameDetailRoute(gameId)`), tanto via esquema customizado `pearl://` quanto via `https://pearl.gg`:

* **Esquemas configurados no `AndroidManifest.xml` e `NavHost`:**
  * `pearl://game/{gameId}`
  * `https://pearl.gg/game/{gameId}`

* **Comando `adb` para testar no emulador/dispositivo Android:**
```bash
adb shell am start -a android.intent.action.VIEW -d "pearl://game/gta-vi"
```
Ou testando outro jogo do catálogo:
```bash
adb shell am start -a android.intent.action.VIEW -d "pearl://game/gta-seis-pc"
```

---

## ♿ Acessibilidade e Revisão com Leitor de Tela / TalkBack (T8)

Durante a revisão das telas com foco em leitores de tela (TalkBack) e precisão de toque, foram realizados os seguintes ajustes no módulo `:mobile`:

1. **Agrupamento semântico de notas em estrelas (`clearAndSetSemantics`):**
   * *Antes:* Em `ReviewCard`, as 5 estrelas eram anunciadas individualmente pelo leitor de tela (*"Estrela preenchida, Estrela preenchida, Estrela preenchida, Estrela vazia..."*).
   * *Ajuste:* A `Row` de estrelas foi agrupada com `.clearAndSetSemantics { contentDescription = "Nota: $stars de 5 estrelas" }`, anunciando a nota completa de uma só vez.
2. **Alvos de toque mínimos de `48.dp`:**
   * *Antes:* O botão de abrir o perfil do autor em `ReviewCard` possuía `Modifier.size(28.dp)`, abaixo do mínimo recomendado de `48.dp`.
   * *Ajuste:* A linha inteira com ícone e nome do autor foi unificada com `Modifier.heightIn(min = 48.dp)`, `semantics(mergeDescendants = true)` e `Role.Button`, garantindo alvo de toque confortável e anúncio único (*"Avaliado por [usuário]. Toque para abrir o perfil"*). No seletor de estrelas de `CreateReviewScreen`, cada estrela também possui área clicável de `48.dp` com `Role.RadioButton`.
3. **Linhas de alternância agrupadas (`toggleable` com `Role.Switch`):**
   * Na tela de formulário `CreateReviewScreen`, a opção *"Esta avaliação contém spoilers"* agrupa o texto explicativo e o `Switch` na mesma `Row` usando `Modifier.toggleable(value = hasSpoilers, role = Role.Switch, onValueChange = ...)` e `onCheckedChange = null` no `Switch` filho.
4. **Cabeçalhos navegáveis (`heading()`):**
   * Todos os títulos principais de tela (`AppBar`) e cabeçalhos de seção (`"Jogos em alta"`, `"Avaliações recentes"`, `"Top 10 mais avaliados do mês"`, `"Sobre mim"`, `"Últimas avaliações"`, `"Sinopse"`) foram marcados com `Modifier.semantics { heading() }`.
5. **Contraste e Papéis do Tema Material 3 (`PearlTheme`):**
   * Foram definidos esquemas completos de cores para os modos **Claro** e **Escuro** em `Theme.kt`, substituindo cores fixas por papéis semânticos (`surface`, `surfaceContainer`, `onSurface`, `primary`, `primaryContainer`, `onSurfaceVariant`, `error`).

---

## 📄 Licença

Este projeto está licenciado sob a [Licença MIT](LICENSE).
