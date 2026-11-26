# ADR-0001: Microsserviço `ingestor-go` e Comunicação gRPC com Fallback

- **Status:** Aceita
- **Data:** 2026-11-25

## Contexto

O Pearl.gg precisa consultar um catálogo extenso e atualizado de jogos na API externa da **IGDB (Twitch)** — incluindo busca por título, empresas envolvidas, sinopse, capa e múltiplas plataformas — enquanto mantém o serviço principal em **Kotlin/Ktor** focado nas regras de negócio sociais, autenticação e persistência transacional no **PostgreSQL**.

Acoplar chamadas HTTP externas e normalização de payloads da IGDB diretamente no fluxo síncrono do backend Ktor traria três problemas:
1. Acoplamento do domínio relacional às peculiaridades de schema e autenticação da IGDB.
2. Dificuldade de evoluir ou substituir o provedor de catálogo externo sem alterar regras de negócio.
3. Risco de indisponibilidade ou lentidão da API externa degradar toda a API principal, inclusive buscas no catálogo já persistido.

## Decisão

Adotamos uma arquitetura de dois serviços comunicando-se via **gRPC** com contrato versionado em **Protocol Buffers**:

1. **Separação de responsabilidades (`ingestor-go` vs. `backend`):**
   - `services/ingestor-go/` (Go) atua como gateway de ingestão para a IGDB, traduzindo o payload externo (incluindo montagem da URL completa da capa e normalização das dezenas de plataformas da IGDB para os valores aceitos pela constraint `chk_game_platform` do PostgreSQL: `PC`, `PLAYSTATION`, `XBOX` e `NINTENDO`).
   - `backend/` (Kotlin/Ktor) permanece como única autoridade sobre usuários, avaliações (`posts`), comentários e seguidores.

2. **Contrato versionado e validado com Buf (`protos/pearl/catalog/v1/game.proto`):**
   - Contrato centralizado na raiz (`protos/`) sob o pacote `pearl.catalog.v1`, validado com `buf lint` (`STANDARD` + `COMMENTS`).
   - Os stubs em Go (`services/ingestor-go/proto/`) e em Kotlin/Java (`backend/build.gradle.kts` com `com.google.protobuf` e `grpc-kotlin-stub`) são gerados a partir da mesma fonte única.

3. **Arquitetura Hexagonal e Isolamento no Backend:**
   - O domínio define a porta `GameCatalogPort` e o resultado selado `CatalogSearchResult` (`Success` / `Unavailable`) em `gg.pearl.model.port`, sem qualquer dependência de `io.grpc` ou `com.google.protobuf`.
   - O adaptador `GrpcGameCatalogAdapter` em `gg.pearl.infrastructure.grpc` encapsula o stub `GameServiceCoroutineStub` e é verificado por regras arquiteturais do **Konsist** (`ArchitectureTest`).

4. **Resiliência, Timeout Explícito e Persistência sob Demanda:**
   - Toda chamada gRPC aplica deadline por requisição (`withDeadlineAfter(deadlineMs, MILLISECONDS)`).
   - Em caso de `UNAVAILABLE` ou `DEADLINE_EXCEEDED` no microsserviço Go, `GameService.searchGames` realiza **fallback automático** para o PostgreSQL (`X-Catalog-Source: postgres-fallback`), garantindo que o endpoint `GET /games/search` funcione mesmo sem o `ingestor-go`.
   - Jogos retornados pela busca externa não são gravados automaticamente no banco; a persistência ocorre **sob demanda** via `GameRepository.findOrCreateByExternalId` apenas quando um usuário cria uma avaliação (`POST /posts`) vinculada ao jogo.

## Alternativas Consideradas

- **Chamar a IGDB diretamente do Ktor via HTTP Client:** rejeitada por misturar integração externa e normalização de catálogo no núcleo transacional e fugir da divisão definida na proposta do projeto.
- **Comunicação REST/JSON entre Ktor e Go:** rejeitada em favor de gRPC + Protobuf pela tipagem forte compartilhada em contrato único, menor overhead de serialização e suporte nativo a `withDeadlineAfter` e `grpc_health_v1`.
- **Persistir todos os resultados de busca no PostgreSQL imediatamente:** rejeitada para evitar poluição do catálogo local com jogos nunca avaliados e escrita desnecessária durante buscas exploratórias.

## Consequências

### Positivas
- Contrato único fortemente tipado entre Go e Kotlin com detecção de quebras em build/CI.
- API principal permanece operacional mesmo com queda ou lentidão do microsserviço Go ou da IGDB.
- Catálogo local no PostgreSQL cresce apenas com jogos efetivamente referenciados por avaliações, mantendo integridade referencial e performance.

### Negativas / Trade-offs
- Exige geração de código Protobuf nos builds de Go (`buf generate`) e Gradle (`generateProto`).
- Requer manutenção de testes em ambos os lados do contrato (`bufconn` no Go e `grpc-inprocess` no Kotlin).
