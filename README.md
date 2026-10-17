# Pearl.gg — Monorepo

Repositório unificado da plataforma **Pearl.gg**, desenvolvido como projeto integrado para as disciplinas:
* **DIM0547 — Desenvolvimento de Sistemas Web II**
* **DIM0524 — Desenvolvimento de Sistemas para Dispositivos Móveis**

---

## 🎥 Vídeos de Demonstração — Sprint 1 (T10)

* 🌐 **Web II (DIM0547 — Backend Kotlin/Ktor, PostgreSQL, Flyway, Clean Architecture, Problem Details e OpenAPI):**  
  👉 [Assistir no YouTube (https://youtu.be/-nqagS4l5iU)](https://youtu.be/-nqagS4l5iU)
* 📱 **Dispositivos Móveis (DIM0524 — Compose Multiplatform, Rotas Tipadas, Deep Link, Adaptatividade e Acessibilidade):**  
  👉 [Assistir no YouTube (https://youtu.be/kVFMg3hMrSY)](https://youtu.be/kVFMg3hMrSY)

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

### 5. Aplicativo Móvel — KMP / Compose Multiplatform (DIM0524)

Para executar a versão Desktop (com *Hot Reload* / redimensionamento de janela para testar as 2 classes de largura):
```bash
./gradlew :mobile:run
```

Para executar os testes unitários do módulo Mobile (incluindo validação pura de formulários):
```bash
./gradlew :mobile:desktopTest
```

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

