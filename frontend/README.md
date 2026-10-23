# Frontend – Passe

Interface do sistema de gestão da liga, feita com React, TypeScript e Vite. O passo a passo completo
(banco, API e interface, inclusive com Docker Compose) está no [README da raiz](../README.md).

## Stack

- React 19 + TypeScript, projeto criado com Vite
- Tailwind CSS 4 + [shadcn/ui](https://ui.shadcn.com) (componentes em `src/components/ui`) e ícones Lucide
- React Router (modo biblioteca) para as rotas
- TanStack Query para as chamadas à API
- React Hook Form + Zod para os formulários
- Vitest + Testing Library (testes de componente)
- ESLint + Prettier (lint e formatação)

## Requisitos

- Node.js 22 LTS (também funciona com Node.js 20.19 ou superior)
- Backend rodando (veja `../backend/README.md`)

## Configuração

A origem da API fica na variável `VITE_API_URL`, já com o prefixo `/api/v1`. O valor padrão está no
`.env` versionado, e o modelo, no `.env.example`:

| Variável       | Padrão                         |
| -------------- | ------------------------------ |
| `VITE_API_URL` | `http://localhost:8080/api/v1` |

Para usar outro endereço, crie um `.env.local` (ignorado pelo Git) com o novo valor.

## Executar

Na pasta `frontend`:

```bash
npm ci
npm run dev
```

Interface em http://localhost:5173.

## Docker

O `Dockerfile` gera o build de produção (com `VITE_API_URL` informado como argumento de build) e o
serve com Nginx (`nginx.conf`, que devolve o `index.html` para as rotas da SPA). Ele é usado pelo
`docker-compose.yml` da raiz.

## Scripts

| Comando                | O que faz                                              |
| ---------------------- | ------------------------------------------------------ |
| `npm run dev`          | Sobe o servidor de desenvolvimento na porta 5173       |
| `npm run build`        | Verifica os tipos e gera o build de produção em `dist` |
| `npm run preview`      | Serve o build de produção na porta 5173                |
| `npm test`             | Executa os testes uma vez e termina                    |
| `npm run test:watch`   | Executa os testes em modo de observação                |
| `npm run lint`         | Verifica o código com o ESLint                         |
| `npm run format:check` | Verifica a formatação com o Prettier, sem alterar      |
| `npm run format`       | Formata os arquivos com o Prettier                     |

## Organização

```
src/
  app/            rotas, caminhos das telas e breadcrumb
  components/
    common/       componentes compartilhados (estados de lista, filtros, diálogos, paginação)
    layout/       menu lateral e cabeçalho
    ui/           componentes do shadcn/ui
  features/
    players/      jogadores: lista, filtros, cadastro, edição, condição, exclusão e detalhes
    clubs/        clubes: lista, criação e página do clube com o elenco
    market/       compra, venda e histórico de transações com CSV
    league/       escalação, partidas, duelos e classificação
  lib/            cliente da API, formatação de valores e datas, validação de faixas e texto
  test/           configuração e utilitários dos testes
```

Cada área em `features/` tem o seu `api.ts` (consultas e ações com TanStack Query), os tipos da API,
as regras de validação e os componentes. Os testes ficam ao lado do código (`*.test.ts(x)`).
