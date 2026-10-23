package br.com.footballmarket.market.api;

import br.com.footballmarket.PostgresIntegrationTest;
import br.com.footballmarket.shared.time.BrasiliaTime;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Compra com banco real: o que é gravado numa única operação, o contrato no elenco do clube
 * e as travas contra compras simultâneas.
 */
class MarketApiIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void cleanDatabase() {
        jdbc.execute("TRUNCATE transacoes, contratos, jogadores, clubes RESTART IDENTITY CASCADE");
    }

    private int createClub(String name, String acronym) throws Exception {
        String body = mvc.perform(post("/api/v1/clubs").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + name + "\", \"acronym\": \"" + acronym + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private int registerMeiaLua(String name) throws Exception {
        String body = mvc.perform(post("/api/v1/players").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "%s", "position": "MEI", "agility": 85, "age": 25,
                         "pace": 70, "shooting": 68, "passing": 82, "dribbling": 79, "defending": 55,
                         "physical": 64, "potential": 86}
                        """.formatted(name)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    /**
     * Fim da fidelidade a {@code days} dias do minuto atual, no formato da RN01.
     */
    private static String loyaltyIn(int days) {
        return BrasiliaTime.format(OffsetDateTime.now(BrasiliaTime.OFFSET).truncatedTo(ChronoUnit.MINUTES).plusDays(days))
                .substring(0, 16) + "-03:00";
    }

    private int buyStatus(int playerId, int clubId) throws Exception {
        return mvc.perform(post("/api/v1/players/" + playerId + "/buy").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"club_id\": " + clubId + ", \"loyalty_until\": \"" + loyaltyIn(120) + "\"}"))
                .andReturn().getResponse().getStatus();
    }

    @Test
    void buyRecordsEverythingInOneOperation() throws Exception {
        int club = createClub("Águias FC", "AGU");
        int player = registerMeiaLua("Meia Lua");
        mvc.perform(patch("/api/v1/players/" + player + "/condition").contentType(MediaType.APPLICATION_JSON)
                .content("{\"condition\": \"MAL_CONDICIONADO\"}")).andExpect(status().isOk());

        mvc.perform(post("/api/v1/players/" + player + "/buy").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"club_id\": " + club + ", \"loyalty_until\": \"" + loyaltyIn(120) + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(37.00))
                .andExpect(jsonPath("$.fee").value(1.85))
                .andExpect(jsonPath("$.cash_change").value(-38.85))
                .andExpect(jsonPath("$.club_cash").value(261.15))
                .andExpect(jsonPath("$.contract.status").value("ATIVO"));

        assertThat((BigDecimal) jdbc.queryForObject("SELECT caixa FROM clubes WHERE id = ?", BigDecimal.class, club))
                .isEqualByComparingTo("261.15");
        assertThat(jdbc.queryForObject("SELECT clube_id FROM jogadores WHERE id = ?", Integer.class, player))
                .isEqualTo(club);
        Map<String, Object> contract = jdbc.queryForMap("SELECT * FROM contratos WHERE jogador_id = ?", player);
        assertThat(contract.get("situacao")).isEqualTo("ATIVO");
        assertThat((BigDecimal) contract.get("valor_compra")).isEqualByComparingTo("37.00");
        assertThat(contract.get("encerrado_em")).isNull();
        Map<String, Object> transaction = jdbc.queryForMap("SELECT * FROM transacoes WHERE jogador_id = ?", player);
        assertThat(transaction.get("tipo")).isEqualTo("COMPRA");
        assertThat(transaction.get("nome_jogador")).isEqualTo("Meia Lua");
        assertThat(transaction.get("lucro")).isNull();
        assertThat((BigDecimal) transaction.get("variacao_caixa")).isEqualByComparingTo("-38.85");
        // RN17: 300,00 + soma de cash_change = caixa atual
        assertThat(jdbc.queryForObject(
                "SELECT 300 + SUM(variacao_caixa) = (SELECT caixa FROM clubes WHERE id = ?) FROM transacoes WHERE clube_id = ?",
                Boolean.class, club, club)).isTrue();

        mvc.perform(get("/api/v1/clubs/" + club))
                .andExpect(jsonPath("$.squad_value").value(37.00))
                .andExpect(jsonPath("$.net_worth").value(298.15))
                .andExpect(jsonPath("$.players[0].contract.purchase_value").value(37.00))
                .andExpect(jsonPath("$.players[0].contract.remaining_days").value(120))
                .andExpect(jsonPath("$.players[0].contract.penalty_rate").value(10));

        // jogador com clube não pode ser excluído nem comprado de novo
        mvc.perform(post("/api/v1/players/" + player + "/buy").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"club_id\": " + club + ", \"loyalty_until\": \"" + loyaltyIn(30) + "\"}"))
                .andExpect(status().isConflict());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM transacoes", Integer.class)).isEqualTo(1);
    }

    @Test
    void refusedBuyRecordsNothing() throws Exception {
        int club = createClub("Águias FC", "AGU");
        int player = registerMeiaLua("Meia Lua");
        jdbc.update("UPDATE clubes SET caixa = 83.99 WHERE id = ?", club); // custo 42,00 > 41,995

        assertThat(buyStatus(player, club)).isEqualTo(409);

        assertThat(jdbc.queryForObject("SELECT caixa FROM clubes WHERE id = ?", BigDecimal.class, club))
                .isEqualByComparingTo("83.99");
        assertThat(jdbc.queryForObject("SELECT clube_id FROM jogadores WHERE id = ?", Integer.class, player)).isNull();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM contratos", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM transacoes", Integer.class)).isZero();
    }

    @Test
    void concurrentBuysOfTheSamePlayerLetOnlyOneThrough() throws Exception {
        int aguias = createClub("Águias FC", "AGU");
        int bravos = createClub("Bravos FC", "BFC");
        int player = registerMeiaLua("Meia Lua");

        List<Integer> statuses = runTogether(List.of(() -> buyStatus(player, aguias), () -> buyStatus(player, bravos)));

        assertThat(statuses).containsExactlyInAnyOrder(200, 409);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM contratos WHERE jogador_id = ?", Integer.class, player))
                .isEqualTo(1);
    }

    @Test
    void concurrentBuysDoNotOverflowTheSquad() throws Exception {
        int club = createClub("Águias FC", "AGU");
        jdbc.update("UPDATE clubes SET caixa = 99999999.99 WHERE id = ?", club);
        for (int i = 0; i < 11; i++) {
            assertThat(buyStatus(registerMeiaLua("Jogador " + (char) ('A' + i)), club)).isEqualTo(200);
        }
        int twelfth = registerMeiaLua("Jogador Doze");
        int thirteenth = registerMeiaLua("Jogador Treze");

        List<Integer> statuses = runTogether(List.of(() -> buyStatus(twelfth, club), () -> buyStatus(thirteenth, club)));

        assertThat(statuses).containsExactlyInAnyOrder(200, 409);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM jogadores WHERE clube_id = ?", Integer.class, club))
                .isEqualTo(12);
    }

    private int sellStatus(int playerId) throws Exception {
        return mvc.perform(post("/api/v1/players/" + playerId + "/sell")).andReturn().getResponse().getStatus();
    }

    private void assertCashMatchesTransactions(int club) {
        // RN17: 300,00 + soma de cash_change = caixa atual
        assertThat(jdbc.queryForObject(
                "SELECT 300 + COALESCE(SUM(variacao_caixa), 0) = (SELECT caixa FROM clubes WHERE id = ?) "
                        + "FROM transacoes WHERE clube_id = ?", Boolean.class, club, club)).isTrue();
    }

    @Test
    void sellFreesThePlayerAndBlocksTheRebuyOnlyForTheSeller() throws Exception {
        int aguias = createClub("Águias FC", "AGU");
        int bravos = createClub("Bravos FC", "BFC");
        int player = registerMeiaLua("Meia Lua");
        assertThat(buyStatus(player, aguias)).isEqualTo(200); // 40,00 + 2,00 de taxa; 120 dias de fidelidade

        // vendido logo depois: 120 dias restantes → 10% de 40,00; sem lucro, sem comissão
        mvc.perform(post("/api/v1/players/" + player + "/sell"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("VENDA"))
                .andExpect(jsonPath("$.value").value(40.00))
                .andExpect(jsonPath("$.penalty").value(4.00))
                .andExpect(jsonPath("$.profit").value(0.00))
                .andExpect(jsonPath("$.fee").value(0.00))
                .andExpect(jsonPath("$.cash_change").value(36.00))
                .andExpect(jsonPath("$.remaining_days").value(120))
                .andExpect(jsonPath("$.contract_status").value("RESCINDIDO"))
                .andExpect(jsonPath("$.club_cash").value(294.00));

        assertThat(jdbc.queryForObject("SELECT clube_id FROM jogadores WHERE id = ?", Integer.class, player)).isNull();
        Map<String, Object> contract = jdbc.queryForMap("SELECT * FROM contratos WHERE jogador_id = ?", player);
        assertThat(contract.get("situacao")).isEqualTo("RESCINDIDO");
        assertThat(contract.get("encerrado_em")).isNotNull();
        assertThat((BigDecimal) jdbc.queryForObject("SELECT lucro FROM transacoes WHERE tipo = 'VENDA'", BigDecimal.class))
                .isEqualByComparingTo("0.00");
        assertCashMatchesTransactions(aguias);

        assertThat(sellStatus(player)).isEqualTo(409);     // agora está livre
        assertThat(buyStatus(player, aguias)).isEqualTo(409); // o Águias já vendeu este jogador
        assertThat(buyStatus(player, bravos)).isEqualTo(200); // outro clube pode comprar
        assertCashMatchesTransactions(bravos);
        mvc.perform(get("/api/v1/clubs/" + aguias)).andExpect(jsonPath("$.players").isEmpty());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM transacoes", Integer.class)).isEqualTo(3);
    }

    @Test
    void fulfilledLoyaltyEndsTheContractWithoutPenalty() throws Exception {
        int club = createClub("Águias FC", "AGU");
        int player = registerMeiaLua("Meia Lua");
        assertThat(buyStatus(player, club)).isEqualTo(200);
        jdbc.update("UPDATE contratos SET iniciado_em = now() - interval '10 days', "
                + "fidelidade_ate = now() - interval '1 day' WHERE jogador_id = ?", player);

        mvc.perform(post("/api/v1/players/" + player + "/sell"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remaining_days").value(0))
                .andExpect(jsonPath("$.penalty").value(0.00))
                .andExpect(jsonPath("$.contract_status").value("ENCERRADO"));
        assertThat(jdbc.queryForObject("SELECT situacao FROM contratos WHERE jogador_id = ?", String.class, player))
                .isEqualTo("ENCERRADO");
        assertCashMatchesTransactions(club);
    }

    @Test
    void concurrentSalesOfTheSamePlayerLetOnlyOneThrough() throws Exception {
        int club = createClub("Águias FC", "AGU");
        int player = registerMeiaLua("Meia Lua");
        assertThat(buyStatus(player, club)).isEqualTo(200);

        List<Integer> statuses = runTogether(List.of(() -> sellStatus(player), () -> sellStatus(player)));

        assertThat(statuses).containsExactlyInAnyOrder(200, 409);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM transacoes WHERE tipo = 'VENDA'", Integer.class))
                .isEqualTo(1);
        assertCashMatchesTransactions(club);
    }

    private static List<Integer> runTogether(List<Callable<Integer>> tasks) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        try {
            CountDownLatch start = new CountDownLatch(1);
            List<Future<Integer>> futures = new ArrayList<>();
            for (Callable<Integer> task : tasks) {
                futures.add(executor.submit(() -> {
                    start.await();
                    return task.call();
                }));
            }
            start.countDown();
            List<Integer> results = new ArrayList<>();
            for (Future<Integer> future : futures) {
                results.add(future.get());
            }
            return results;
        } finally {
            executor.shutdownNow();
        }
    }
}
