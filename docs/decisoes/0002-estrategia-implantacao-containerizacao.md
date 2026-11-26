# ADR-0002: Estratégia de Containerização, Orquestração Local e Pipeline de CI

- **Status:** Aceita
- **Data:** 2026-11-25

## Contexto

Com a introdução do microsserviço `ingestor-go` comunicando-se com o backend Ktor via gRPC sobre a rede local e com a dependência compartilhada dos contratos `protos/`, tornou-se necessário padronizar:
1. O empacotamento reprodutível de ambos os serviços em imagens mínimas.
2. A ordem de inicialização entre PostgreSQL, `ingestor-go` e `backend` no ambiente Docker local.
3. A validação automatizada contínua do contrato Protobuf, dos dois serviços e do app mobile.

## Decisão

1. **Dockerfiles multi-stage para ambos os serviços:**
   - **Backend (`backend/Dockerfile`):** estágio de build com `eclipse-temurin:21-jdk` (compatível com `glibc` exigida pelos binários `protoc` e `protoc-gen-grpc-java`) copiando explicitamente `protos/` junto ao código-fonte para permitir a geração dos stubs gRPC-Kotlin em `installDist`, e estágio de execução enxuto com `eclipse-temurin:21-jre-alpine`.
   - **Microsserviço Go (`services/ingestor-go/Dockerfile`):** estágio de build com `golang:1.26-alpine` compilando binário estático (`CGO_ENABLED=0`) e estágio de execução mínimo com `alpine:3.21`, usuário não-root (`appuser`), `ca-certificates` (necessário para chamadas TLS à API da IGDB) e probe nativo `-healthcheck`.

2. **Health Check gRPC nativo e `depends_on: service_healthy`:**
   - O `server/main.go` do `ingestor-go` expõe o serviço padrão `grpc.health.v1.Health` (`SERVING` para `""` e `pearl.catalog.v1.GameService`) e trata `SIGINT`/`SIGTERM` com `GracefulStop()`.
   - O modo `-healthcheck` do próprio binário realiza uma checagem gRPC em `127.0.0.1:${GRPC_PORT}` sem exigir ferramentas extras no container Alpine.
   - No `docker-compose.yml`, o serviço `backend` declara `depends_on` com `condition: service_healthy` tanto para `postgres` (`pg_isready`) quanto para `ingestor-go` (`-healthcheck`), recebendo `INGESTOR_GRPC_HOST=ingestor-go` e `INGESTOR_GRPC_PORT=8081`.

3. **Configuração externalizada e migrações versionadas:**
   - Credenciais e parâmetros (`DB_URL`, `DB_USER`, `DB_PASSWORD`, `CLIENT_ID_TWITCH`, `ACCESS_TOKEN`, `INGESTOR_GRPC_*`) são injetados via variáveis de ambiente e documentados em `.env.example` (com `.env` ignorado pelo Git).
   - O esquema do PostgreSQL evolui por migrações Flyway (`V1__initial_schema.sql`, `V2__add_game_metadata_columns.sql`) executadas automaticamente na inicialização do backend.

4. **Pipeline de CI unificado (`.github/workflows/ci.yml`):**
   - `proto-lint`: executa `buf lint` em `protos/` garantindo conformidade `STANDARD` e `COMMENTS`.
   - `ingestor-go`: usa `go-version-file: services/ingestor-go/go.mod` e executa `go test -v -race ./...` (cobrindo normalização de plataformas, client HTTP e servidor gRPC com `bufconn`).
   - `backend-ktor`: executa `./gradlew :backend:test --no-daemon` (geração Protobuf, Konsist, `grpc-inprocess`, testes unitários e Testcontainers).
   - `mobile-kmp`: executa `./gradlew :mobile:desktopTest --no-daemon`.

5. **Caminho de evolução para implantação gerenciada:**
   - As imagens multi-stage, migrações Flyway e configuração por variáveis de ambiente permitem publicar o backend Ktor e o `ingestor-go` em plataformas gerenciadas (conectados a um PostgreSQL gerenciado como Neon, com segredos configurados no provedor/CI) sem alterações no código da aplicação.

## Alternativas Consideradas

- **Subir o `ingestor-go` apenas com `restart: unless-stopped` sem health check:** rejeitada porque o `backend` poderia tentar conectar antes do servidor gRPC estar pronto ou não sinalizaria degradação ao orquestrador.
- **Instalar `grpc_health_probe` externo no Dockerfile do Go:** rejeitada em favor da flag `-healthcheck` embutida no próprio binário, evitando downloads extras de terceiros e mantendo a imagem final enxuta.

## Consequências

### Positivas
- Subida local com único comando (`docker compose up --build -d`) respeitando dependências reais de saúde.
- CI valida contrato, backend, microsserviço Go (com detector de race condition) e cliente mobile em paralelo.
- Ausência total de segredos versionados no repositório.

### Negativas / Trade-offs
- Em ambientes de CI sem Docker daemon disponível, os testes em `PostgresIntegrationTest` fazem skip seguro em tempo de execução, exigindo ambiente com Docker ativo para validar as asserções contra PostgreSQL real.
