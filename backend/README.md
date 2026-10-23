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
- **Listagem de jogadores (RN07):** o banco aplica os filtros sobre colunas gravadas (posição, condição, livre/clube, clube, idade e exclusão lógica). A busca sem acentos, a faixa de valor de mercado e a ordenação são feitas na aplicação, porque o valor de mercado é calculado a cada consulta e não é gravado; a paginação vem por último. Assim o filtro e a ordenação usam exatamente o mesmo cálculo e arredondamento da resposta. A troca é carregar todos os jogadores que passam pelos filtros do banco, o que é aceitável no tamanho desta liga (no máximo 20 clubes de 12 jogadores, mais os livres).
- **Ordem por nome:** segue a regra das Premissas, para jogadores e clubes: minúsculas, sem acentos (NFD sem as marcas Mn) e comparação por ponto de código Unicode. Nomes iguais nessa comparação são desempatados pelo id.
- **Busca:** o termo passa pela mesma normalização dos nomes (V4) antes de medir os 2 a 100 caracteres. A comparação ignora maiúsculas, minúsculas e acentos, com a mesma definição de acento da V4.
- **Unicidade do nome do clube (RN10):** garantida no banco por uma coluna `clubes.nome_chave` com `UNIQUE`, preenchida pela aplicação com o nome (já normalizado pela V4) em minúsculas. Não usei `lower()` do PostgreSQL porque o resultado depende do locale do servidor. A sigla tem `UNIQUE` própria.
- **Limite de 20 clubes:** a criação trava a tabela `clubes` (`LOCK TABLE ... SHARE ROW EXCLUSIVE`) até o fim da transação, para que duas criações simultâneas não passem juntas do limite. Leituras continuam liberadas.
- **Ordem das validações na criação de clube:** formato do nome e da sigla (400) antes do limite de clubes, do nome repetido e da sigla repetida (409).
- **Sigla:** não passa pela normalização da V4, que vale para nomes e busca. `" AGU"` é recusada (400).
- **Elenco no `GET /clubs/{id}`:** por enquanto cada jogador traz os campos de `GET /players/{id}`; o `contract` entra com a compra (Etapa 5).
