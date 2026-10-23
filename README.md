# Passe — mercado de jogadores e liga

Sistema do estudo de caso do SENAI/PE (Desenvolvedor Fullstack I): cadastro de jogadores, clubes, compra
e venda com contrato de fidelidade, histórico de transações com exportação CSV, escalação, partidas e
classificação.

- **Backend:** Java 21 + Spring Boot 3.5, em [`backend/`](backend/README.md)
- **Frontend:** React 19 + TypeScript + Vite, em [`frontend/`](frontend/README.md)
- **Banco:** PostgreSQL 17, com o schema criado por migrations (Flyway)

## 1. Pré-requisitos

| Ferramenta     | Versão                          | Para quê                                                    |
| -------------- | ------------------------------- | ----------------------------------------------------------- |
| Docker         | 24 ou superior, com Compose v2  | Banco de dados (e, na opção 1, também a API e a interface)  |
| Java (JDK)     | 21                              | Backend, na opção 2, e testes do backend                    |
| Node.js        | 22 LTS (com npm 10)             | Frontend, na opção 2, e testes e lint do frontend           |

O Maven não precisa ser instalado: o backend usa o Maven Wrapper (`mvnw`). O PostgreSQL 17 sobe pelo
Docker Compose.

## 2. Banco de dados

O banco é criado pelo `docker-compose.yml` da raiz (serviço `db`) e o schema é aplicado pelas
migrations do Flyway (`backend/src/main/resources/db/migration`, de `V1` a `V5`) quando a API sobe. O
banco começa vazio: sem jogadores, clubes, transações nem partidas.

**Criar o banco** (na raiz do repositório):

```bash
docker compose up -d db
```

**Recriar do zero** (na raiz do repositório): apaga o volume do banco e cria de novo. As tabelas voltam
na próxima vez que a API subir.

```bash
docker compose down -v
docker compose up -d db
```

Na opção 1 (tudo no Docker), recrie com `docker compose down -v` e `docker compose up -d --build`.

## 3. Instalar e executar (Ubuntu 24.04 com Docker)

- **Origem da API:** `http://localhost:8080` (sem `/api/v1`; as rotas ficam em `http://localhost:8080/api/v1`)
- **Interface:** `http://localhost:5173`

Use **uma** das opções abaixo; as duas usam as mesmas portas.

### Opção 1 — tudo com Docker Compose (um comando)

Na raiz do repositório:

```bash
docker compose up -d --build
```

Sobe o banco, a API e a interface. O primeiro build baixa as dependências e leva alguns minutos. Para
parar: `docker compose down`.

### Opção 2 — API e interface direto na máquina

```bash
# Banco de dados (na raiz do repositório)
docker compose up -d db

# Backend (na pasta backend, num terminal)
cd backend
sh mvnw spring-boot:run
# API em http://localhost:8080 (verificação: http://localhost:8080/actuator/health)

# Frontend (em outro terminal, a partir da raiz do repositório)
cd frontend
npm ci
npm run dev
# Interface em http://localhost:5173
```

### Testes, lint e formatação

```bash
# Backend (na pasta backend; os testes de integração usam Testcontainers, então o Docker precisa estar ativo)
cd backend
sh mvnw test

# Frontend (na pasta frontend)
cd frontend
npm ci
npm test              # Vitest + Testing Library, executa uma vez e termina
npm run lint          # ESLint
npm run format:check  # Prettier, só verifica (não altera arquivos)
```

## 4. Variáveis de ambiente

Todas têm valor padrão: nada precisa ser configurado para rodar os passos acima.

**Docker Compose da raiz** — modelo em [`.env.example`](.env.example). Para trocar algo, copie para
`.env` na raiz (`cp .env.example .env`) e edite.

| Variável      | Padrão    | Uso                                              |
| ------------- | --------- | ------------------------------------------------ |
| `DB_NAME`     | `desafio` | Nome do banco                                    |
| `DB_USERNAME` | `desafio` | Usuário do banco                                 |
| `DB_PASSWORD` | `desafio` | Senha do banco                                   |
| `DB_PORT`     | `5432`    | Porta do banco no computador                     |
| `API_PORT`    | `8080`    | Porta da API no computador (opção 1)             |
| `WEB_PORT`    | `5173`    | Porta da interface no computador (opção 1)       |

**Backend** — lidas do ambiente pelo `application.yml` (detalhes em [`backend/README.md`](backend/README.md)):

| Variável               | Padrão                                     |
| ---------------------- | ------------------------------------------ |
| `DB_URL`               | `jdbc:postgresql://localhost:5432/desafio` |
| `DB_USERNAME`          | `desafio`                                  |
| `DB_PASSWORD`          | `desafio`                                  |
| `SERVER_PORT`          | `8080`                                     |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` (separe por vírgula) |

**Frontend** — modelo em [`frontend/.env.example`](frontend/.env.example); o `frontend/.env` versionado
já traz o padrão:

| Variável       | Padrão                         | Uso                                  |
| -------------- | ------------------------------ | ------------------------------------ |
| `VITE_API_URL` | `http://localhost:8080/api/v1` | Origem da API com o prefixo `/api/v1` |

## 5. Estrutura de pastas e bibliotecas

```
.
├── docker-compose.yml          banco, API e interface (opção 1) ou só o banco (opção 2)
├── .env.example                variáveis do Docker Compose
├── backend/                    API REST (Spring Boot)
│   ├── Dockerfile
│   └── src/main/java/br/com/footballmarket/
│       ├── shared/             configuração, erros, datas, dinheiro e texto (V4)
│       ├── player/             jogadores: cadastro, edição, condição, exclusão e listagem
│       ├── club/               clubes, elenco e escalação
│       ├── market/             compra, venda, histórico de transações e CSV
│       └── league/             partidas, duelos e classificação
│           (cada módulo com api/, application/, domain/ e infra/)
└── frontend/                   interface (React + Vite)
    ├── Dockerfile, nginx.conf
    └── src/
        ├── app/                rotas, caminhos e breadcrumb
        ├── components/
        │   ├── common/         componentes compartilhados (estados de lista, filtros, diálogos, paginação)
        │   ├── layout/         menu lateral e cabeçalho
        │   └── ui/             componentes do shadcn/ui
        ├── features/
        │   ├── players/        jogadores
        │   ├── clubs/          clubes e página do clube
        │   ├── market/         compra, venda e histórico de transações
        │   └── league/         escalação, partidas e classificação
        ├── lib/                cliente da API, formatação de valores e datas, validação de faixas e texto
        └── test/               configuração e utilitários dos testes
```

### Bibliotecas do backend

| Biblioteca                                    | Motivo                                                                 |
| --------------------------------------------- | ---------------------------------------------------------------------- |
| Spring Boot Web                               | API REST e tratamento centralizado de erros                            |
| Spring Data JPA (Hibernate)                   | Acesso ao banco e mapeamento das tabelas                               |
| Spring Boot Validation (Bean Validation)      | Validação dos corpos e parâmetros (400)                                |
| Spring Boot Actuator                          | Endpoint de saúde (`/actuator/health`) para conferir se a API subiu    |
| Flyway (`flyway-core`, `flyway-database-postgresql`) | Migrations versionadas que criam e evoluem o schema             |
| PostgreSQL JDBC                               | Driver do banco                                                        |
| Spring Boot Test, JUnit 5                     | Testes de unidade e de integração                                      |
| Testcontainers (PostgreSQL)                   | Testes de integração contra um PostgreSQL real em container            |

### Bibliotecas do frontend

| Biblioteca                                   | Motivo                                                                  |
| -------------------------------------------- | ----------------------------------------------------------------------- |
| React 19 + TypeScript, Vite                  | Interface com tipagem e build rápido (stack do enunciado)               |
| Tailwind CSS 4                               | Estilos com as cores e espaçamentos do mockup                           |
| shadcn/ui (Radix UI), lucide-react, Geist    | Componentes acessíveis (diálogos, menus, selects, abas), ícones e fonte do mockup |
| Sonner                                       | Notificações (toasts) de sucesso e de erro                              |
| React Router (modo biblioteca)               | Rotas das telas e filtros guardados na URL                              |
| TanStack Query                               | Chamadas à API com cache, estados de carregamento e erro e recarga após cada ação |
| React Hook Form + Zod (`@hookform/resolvers`) | Formulários com validação antes do envio e mensagem junto ao campo     |
| Vitest + Testing Library + jsdom             | Testes de componente e de fluxo, com a API simulada                     |
| ESLint + Prettier                            | Lint e formatação                                                       |

## 6. Decisões e premissas

As decisões do backend (validações, ordem dos erros, concorrência, partidas e classificação) estão em
[`backend/README.md`](backend/README.md#decisões-e-premissas). As do frontend:

- **Valores sempre da API.** A tela não recalcula valor base, valor de mercado, overall, taxa, multa,
  comissão, lucro, crédito, totais, placar nem classificação. O custo total da compra é mostrado a
  partir do `cash_change` devolvido, sem somar valores na tela.
- **Atualização sem recarregar a página.** Depois de cada ação, as consultas relacionadas são
  recarregadas da API (por exemplo, uma venda recarrega jogadores, clube, escalação e histórico).
- **Datas.** Exibidas no horário de Brasília (`America/Sao_Paulo`). A data e a hora digitadas na compra
  são enviadas com `-03:00`; a sugestão inicial da fidelidade é daqui a um ano.
- **Filtros na URL.** Filtros, ordenação, página, abas e diálogos abertos ficam na URL, então recarregar
  a página mantém a tela como estava.
- **Busca.** Espera 300 ms depois da digitação e só é enviada com 2 caracteres ou mais (a API recusa
  textos menores); a tela avisa quando falta letra.
- **Validação antes do envio.** Além dos limites de cada campo, o formulário de jogador confere as regras
  que a API recusaria com 400: agilidade até 85 com 35 anos ou mais, soma dos atributos até 480 e
  potencial em relação ao maior atributo. No clube, a sigla é convertida para maiúsculas e conferida
  contra o nome. As regras de negócio (409) e os recursos inexistentes (404) não são bloqueados: a tela
  envia e mostra o `message` da API.
- **Recusas previsíveis bloqueadas na tela**, como o enunciado permite: “só livres” junto com o filtro
  por clube (nas duas direções), de `DOENTE` direto para `APTO`, mudança de posição de jogador com clube
  e idade menor que a atual. Na escalação, cada vaga só oferece jogadores do elenco na posição certa e
  sem `DOENTE`, e o salvar só é liberado com as 5 vagas preenchidas.
- **Exclusão.** O item “Excluir” só fica ativo para jogador livre, com o motivo visível.
- **Histórico de transações.** O último tipo marcado não pode ser desmarcado (a API exige ao menos um).
  O filtro por jogador lista os jogadores que aparecem no histórico do clube. O CSV é baixado pela API
  com os filtros da tela, para que uma recusa apareça como mensagem em vez de uma página de erro.
- **Partidas.** O visitante não pode ser o mesmo clube do mandante (a lista exclui o mandante).
- **Envio único.** Todo botão de ação fica desabilitado durante o envio, e os diálogos não fecham no
  meio dele, evitando pedidos duplicados.

## 7. Funcionalidades não implementadas

Todas as funcionalidades obrigatórias de backend e de frontend foram implementadas. Itens opcionais que
ficaram de fora:

- **Responsividade em 375 px** (diferencial de interface): as telas foram pensadas para 1366 × 768.
- **Tela inicial e campo com os dois times na partida** do mockup, que o enunciado marca como opcionais.
- **Documentação interativa (Swagger), log de cada requisição e lint no backend** (diferenciais técnicos).

## 8. Uso de inteligência artificial

Utilizei o **Claude Code** (Anthropic, modelo Claude) como assistente de programação no backend e no
frontend, sempre por etapas, revisando e aprovando cada uma antes de seguir.

- **Onde:** no backend, principalmente nas migrations e nos testes (Li e aprovei um a um);
  no frontend, criei boa parte dos componentes a mão, porém não consegui criar todas as telas a tempo de finalização do projeto, então usei IA para gerar boa parte delas; também utilizei para gerar os testes de componente.
- **Como e por quê:** defini a stack e o plano de etapas e relizei boa parte a mão; a IA propôs e o código das telas faltantes, e testes. A IA rodou os testes e conferiu as telas contra a API real. Usei
  para ganhar velocidade no prazo do desafio, mantendo as decisões de arquitetura e de produto comigo.
- **O que aceitei, alterei ou descartei:** aceitei a criação de telas e componentes faltantes e comentários explicativos a cerca das regras de negócio adicionado no backend. 
  antes do envio e os testes. Pedi ajustes ao longo das etapas (por exemplo, a ordem das entregas e o
  que fica fora do escopo) e deixei de fora a responsividade em 375 px.
