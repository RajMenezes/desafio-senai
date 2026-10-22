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
