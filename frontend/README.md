# Frontend – Passe

Interface do sistema de gestão da liga, feita com React, TypeScript e Vite.

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

A origem da API fica na variável `VITE_API_URL`, já com o prefixo `/api/v1`. O valor padrão está no arquivo `.env`:

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
  app/            rotas, providers e páginas gerais
  components/
    common/       componentes compartilhados (cabeçalho de página, estados de lista, confirmação)
    layout/       menu lateral e cabeçalho
    ui/           componentes do shadcn/ui
  features/       uma pasta por área do sistema (jogadores, clubes, liga)
  lib/            cliente da API, formatação de valores e datas, notificações
```
