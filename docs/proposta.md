# Proposta de Projeto: Pearl.gg

## 1. Visão do Produto

* **Para:** entusiastas do mundo dos games
* **Que:** querem realizar avaliações multiplataforma
* **O Pearl.gg:** é uma rede social
* **Que:** unifica avaliações de jogos
* **Diferente de:** fóruns dispersos e das lojas individuais das plataformas
* **Nosso produto:** padroniza critérios de avaliação de jogos e unifica em um só lugar diversas plataformas.

---

## 2. Definição do MVP

### Dentro do Escopo (In Scope)
* **Perfis:** Criação e visualização de perfis de usuário e páginas dedicadas a jogos;
* **Conexões sociais:** Funcionalidade de seguir outros usuários;
* **Sistema de Avaliação:** Avaliações estruturadas com critérios discriminados e feedback visual marcante (ex.: sistema de estrelas inspirado no Letterboxd);
* **Feed / Comunidade:** Aba geral para postagens da comunidade;
* **Interações:** Comentários em postagens e reviews;
* **Rankings & Destaques:** Seção de *Top Jogos* (mais curtidos, mais comentados e melhores médias de avaliação);
* **Busca e Filtros:** Filtragem de catálogo por plataforma, ano de lançamento, gênero e atributos similares.

### Fora do Escopo (Out of Scope)
* Algoritmos de recomendação de postagens/feed algorítmico;
* Sistema de perfilamento ou recomendação automatizada de amigos;
* Contagem/rastreamento de jogadores ativos simultâneos;
* Recomendação e integração de vídeos, lives ou conteúdos externos de criadores;
* Sistema de mensagens diretas (DMs / chat privado);
* Múltiplos tipos ou níveis de perfil de usuário (ex.: criador, desenvolvedor, assinante).

---

## 3. Link para Backlog Inicial

* **GitHub Projects / Issues:** [Repositório GitHub](https://github.com/Aliragm/pearl/issues)

---

## 4. Entidades Principais do Domínio

* **User (Usuário):** Representa o jogador/membro da rede, contendo dados cadastrais, autenticação, bio, avatar, lista de seguidores e seguidos.
* **Game (Jogo):** Registro de cada título no catálogo, incluindo metadados (título, sinopse, data de lançamento, desenvolvedora/publicadora, plataformas suportadas e mídias visuais).
* **Platform (Plataforma):** Representa as plataformas onde os títulos estão disponíveis (ex.: PC, PlayStation 5, Xbox Series X/S, Nintendo Switch).
* **Review / Rating (Avaliação):** Contém a nota/estrelas (escala visual), análise textual discriminada por critérios técnicos/subjetivos (gameplay, narrativa, gráficos, performance), data, vínculo ao usuário e ao jogo.
* **Post (Postagem):** Publicação no feed geral (discussões gerais, impressões rápidas, listas temáticas).
* **Comment (Comentário):** Interação textual vinculada a postagens ou avaliações de outros usuários.

---

## 5. Decisão de Tecnologia do Serviço Principal: Kotlin com Ktor

* **Decisão:** **Kotlin / Ktor**
* **Justificativa:**  
  A escolha pelo ecossistema Kotlin com Ktor une produtividade, modernidade e aprendizado direcionado. Tendo base prévia em Java, a adoção de Kotlin proporciona um contraste arquitetural rico, aproveitando a maturidade e confiabilidade da JVM enquanto explora recursos contemporâneos da linguagem (como concorrência estruturada via *coroutines*, imutabilidade expressiva e *null-safety* nativo).  
  Como o domínio da plataforma envolve alto volume de dados oriundos de APIs externas de catálogos de jogos, a arquitetura assíncrona, leve e não bloqueante do Ktor oferece a flexibilidade necessária para manter o consumo de recursos sob controle sem o *overhead* de frameworks legados ou mais pesados.

---

## 6. Divisão de Responsabilidades: Serviço Principal (Kotlin) vs. Microsserviço (Go)

A arquitetura adota uma separação estrita entre o núcleo de regras de negócio/persistência relacional e os serviços orientados a alta concorrência e tarefas de I/O intensivo:

| Responsabilidade | Serviço Principal (Kotlin / Ktor) | Microsserviço Auxiliar (Go) |
| :--- | :---: | :---: |
| **Domínio e Regras de Negócio** | Sim | Não |
| **Persistência Relacional e Migrações de Esquema** | Sim | Não |
| **Autenticação e Autorização (JWT / Sessões)** | Sim | Não |
| **Integração com APIs Externas (Catálogo de Jogos)** | Não | Sim |
| **Atualizações sob Demanda & Sync de Dados Externos** | Não | Sim |
| **Camada de Cache de Alto Desempenho (Redis / In-memory)** | Não | Sim |

* **Justificativa:**  
  O serviço em **Kotlin/Ktor** opera como a autoridade central transacional da aplicação, garantindo consistência às regras de negócio sociais (usuários, seguidores, reviews com critérios, moderação e autenticação).  
  O microsserviço em **Go** fica encarregado da ingestão massiva de dados externos, consultas concorrentes resilientes às APIs de games e gerenciamento da camada de cache de alta performance, tirando proveito das *goroutines* e da baixíssima pegada de memória do runtime de Go para rotinas contínuas de I/O e sincronização assíncrona.

---

## 7. Equipe

| Nome                                 | Matrícula     | Papel / Responsabilidades |
|:-------------------------------------|:--------------| :--- |
| **André Lira Gonçalves de Medeiros** | *20230045726* | Backend / Arquitetura de Microsserviços |

---

## 8. Coorte de Apresentação e Integração Curricular

* **Integração de Disciplinas:** Projeto integrado desenvolvido em conjunto para:
    1. **Desenvolvimento de Sistemas Web II**
    2. **Desenvolvimento de Sistemas para Dispositivos Móveis**

---