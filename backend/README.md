# Backend - Desafio ISI Dev

Spring Boot 3.5 · Java 21 · Spring Data JPA · PostgreSQL · Flyway

## Pré-requisitos

- Java 21
- Docker (para o PostgreSQL e para os testes com Testcontainers)

O Maven não precisa estar instalado: use o wrapper (`mvnw` / `mvnw.cmd`).

## Executando

```bash
# 1. Subir o banco
docker compose up -d

# 2. Rodar a aplicação
./mvnw spring-boot:run        # Linux/macOS/Git Bash
mvnw.cmd spring-boot:run      # Windows (cmd/PowerShell)
```

Verificação: `GET http://localhost:8080/actuator/health` → `{"status":"UP"}`

A API fica sob o prefixo `/api/v1`.

## Testes

```bash
./mvnw test
```

## Variáveis de ambiente

| Variável               | Padrão                                     |
|------------------------|--------------------------------------------|
| `DB_URL`               | `jdbc:postgresql://localhost:5432/desafio` |
| `DB_USERNAME`          | `desafio`                                  |
| `DB_PASSWORD`          | `desafio`                                  |
| `SERVER_PORT`          | `8080`                                     |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` (separe por vírgula) |

## Decisões e premissas

Detalhes que o enunciado não definiu (serão consolidados no README da raiz):

- **Datas no banco:** as colunas de data usam `TIMESTAMP WITH TIME ZONE`, que guarda o instante exato; a API converte sempre para `-03:00` (RN01).
- **Ids:** colunas `INTEGER` com identity, coerentes com o limite de 2.147.483.647 dos ids.
- **Clube do jogador:** `jogadores.clube_id` referencia o clube só pelo id (sem associação JPA entre módulos); a FK é criada junto com a tabela `clubes`.
- **Validação em duas camadas:** limites simples (obrigatório, faixas numéricas) ficam no Bean Validation dos DTOs; regras de texto e entre campos (nome com duas palavras, agilidade de veteranos, soma dos atributos, potencial) ficam no domínio. Ambas resultam em 400.
- **Tamanho dos nomes:** contado em pontos de código Unicode, como define a V4 (um emoji ou letra fora do BMP conta como 1 caractere).
- **Condição repetida:** `PATCH /condition` com a condição atual responde 200 sem alterar `atualizado_em`.
