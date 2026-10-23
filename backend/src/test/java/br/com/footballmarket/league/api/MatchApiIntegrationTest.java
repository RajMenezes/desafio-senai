package br.com.footballmarket.league.api;

import br.com.footballmarket.PostgresIntegrationTest;
import br.com.footballmarket.shared.time.BrasiliaTime;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Partidas com banco real, montando pela API os dois elencos do exemplo da RN20.
 */
class MatchApiIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    private int aguias;
    private int delta;
    private int gama;
    private Map<String, Integer> ids;

    @BeforeEach
    void givenTheExampleClubs() throws Exception {
        jdbc.execute("TRUNCATE duelos_partida, partidas, escalacoes, transacoes, contratos, jogadores, clubes "
                + "RESTART IDENTITY CASCADE");
        aguias = createClub("Águias FC", "AGU");
        delta = createClub("Delta FC", "DFC");
        gama = createClub("Gama SC", "GSC");
        ids = new HashMap<>();
        // clube, nome, posição, finalização, passe, drible, defesa
        sign(aguias, "Rui Prado", "GOL", 50, 50, 50, 77);
        sign(aguias, "Davi Lins", "LAT", 50, 50, 74, 66);
        sign(aguias, "Téo Brandão", "ZAG", 50, 50, 50, 84);
        sign(aguias, "Lauro Neves", "MEI", 50, 82, 50, 50);
        sign(aguias, "Otto Rangel", "ATA", 75, 50, 50, 50);
        sign(delta, "Ivo Teles", "GOL", 50, 50, 50, 72);
        sign(delta, "Hugo Vaz", "ZAG", 50, 50, 50, 84);
        sign(delta, "Lino Matos", "LAT", 50, 50, 79, 63);
        sign(delta, "Nei Soares", "MEI", 50, 70, 50, 50);
        sign(delta, "Ugo Falcão", "ATA", 81, 50, 50, 50);
        mvc.perform(patch("/api/v1/players/" + ids.get("Davi Lins") + "/condition").contentType(MediaType.APPLICATION_JSON)
                .content("{\"condition\": \"MAL_CONDICIONADO\"}")).andExpect(status().isOk());
        lineUp(aguias, "Rui Prado", "Davi Lins", "Téo Brandão", "Lauro Neves", "Otto Rangel");
        lineUp(delta, "Ivo Teles", "Hugo Vaz", "Lino Matos", "Nei Soares", "Ugo Falcão");
    }

    private int createClub(String name, String acronym) throws Exception {
        int id = JsonPath.read(mvc.perform(post("/api/v1/clubs").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + name + "\", \"acronym\": \"" + acronym + "\"}"))
                .andReturn().getResponse().getContentAsString(), "$.id");
        jdbc.update("UPDATE clubes SET caixa = 10000 WHERE id = ?", id);
        return id;
    }

    private void sign(int club, String name, String position, int shooting, int passing, int dribbling, int defending)
            throws Exception {
        int id = JsonPath.read(mvc.perform(post("/api/v1/players").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "%s", "position": "%s", "agility": 70, "age": 25, "pace": 50, "shooting": %d,
                         "passing": %d, "dribbling": %d, "defending": %d, "physical": 50, "potential": 99}
                        """.formatted(name, position, shooting, passing, dribbling, defending)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id");
        String loyalty = BrasiliaTime.format(OffsetDateTime.now(BrasiliaTime.OFFSET).truncatedTo(ChronoUnit.MINUTES)
                .plusDays(90)).substring(0, 16) + "-03:00";
        mvc.perform(post("/api/v1/players/" + id + "/buy").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"club_id\": " + club + ", \"loyalty_until\": \"" + loyalty + "\"}"))
                .andExpect(status().isOk());
        ids.put(name, id);
    }

    private void lineUp(int club, String... names) throws Exception {
        String list = String.join(", ", Arrays.stream(names).map(name -> String.valueOf(ids.get(name))).toList());
        mvc.perform(put("/api/v1/clubs/" + club + "/lineup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"formation\": \"2-1-1\", \"player_ids\": [" + list + "]}"))
                .andExpect(status().isOk());
    }

    private ResultActions play(int home, int away) throws Exception {
        return mvc.perform(post("/api/v1/matches").contentType(MediaType.APPLICATION_JSON)
                .content("{\"home_club_id\": " + home + ", \"away_club_id\": " + away + "}"));
    }

    private int count(String table) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }

    @Test
    void playRecordsTheExampleMatch() throws Exception {
        BigDecimal cashBefore = jdbc.queryForObject("SELECT caixa FROM clubes WHERE id = ?", BigDecimal.class, aguias);

        int match = JsonPath.read(play(aguias, delta)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/matches/1")))
                .andExpect(jsonPath("$.home.goals").value(1))
                .andExpect(jsonPath("$.away.goals").value(2))
                .andExpect(jsonPath("$.result").value("VISITANTE"))
                .andExpect(jsonPath("$.duels[*].goal").value(contains(true, false, false, true, false, true)))
                .andExpect(jsonPath("$.duels[2].attacker.value").value(63))
                .andExpect(jsonPath("$.duels[5].defender.value").value(56))
                .andReturn().getResponse().getContentAsString(), "$.id");

        Map<String, Object> row = jdbc.queryForMap("SELECT * FROM partidas WHERE id = ?", match);
        assertThat(row.get("gols_mandante")).isEqualTo(1);
        assertThat(row.get("gols_visitante")).isEqualTo(2);
        assertThat(row.get("formacao_mandante")).isEqualTo("2-1-1");
        List<Map<String, Object>> duels = jdbc.queryForList(
                "SELECT * FROM duelos_partida WHERE partida_id = ? ORDER BY ordem", match);
        assertThat(duels).hasSize(6);
        assertThat(duels.get(2)).containsEntry("nome_atacante", "Davi Lins")
                .containsEntry("condicao_atacante", "MAL_CONDICIONADO")
                .containsEntry("atributo_ataque", "dribbling")
                .containsEntry("valor_ataque", 63)
                .containsEntry("atributo_defesa", "defending")
                .containsEntry("gol", false);

        // sem outros efeitos: caixa, escalação e jogadores continuam iguais
        assertThat(jdbc.queryForObject("SELECT caixa FROM clubes WHERE id = ?", BigDecimal.class, aguias))
                .isEqualByComparingTo(cashBefore);
        assertThat(count("escalacoes")).isEqualTo(10);
        mvc.perform(get("/api/v1/players/" + ids.get("Davi Lins"))).andExpect(jsonPath("$.dribbling").value(74));
    }

    @Test
    void pastMatchesKeepTheDataOfTheMoment() throws Exception {
        int match = JsonPath.read(play(aguias, delta).andReturn().getResponse().getContentAsString(), "$.id");
        mvc.perform(put("/api/v1/players/" + ids.get("Davi Lins")).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Davi Lins Junior\", \"position\": \"LAT\", \"agility\": 70, \"age\": 26}"))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/v1/players/" + ids.get("Davi Lins") + "/condition").contentType(MediaType.APPLICATION_JSON)
                .content("{\"condition\": \"APTO\"}")).andExpect(status().isOk());

        mvc.perform(get("/api/v1/matches/" + match))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duels[2].attacker.player_name").value("Davi Lins"))
                .andExpect(jsonPath("$.duels[2].attacker.condition").value("MAL_CONDICIONADO"))
                .andExpect(jsonPath("$.duels[2].attacker.value").value(63));
    }

    @Test
    void clubWithIncompleteLineupCannotPlay() throws Exception {
        play(aguias, gama).andExpect(status().isConflict());                     // Gama nunca escalou
        mvc.perform(post("/api/v1/players/" + ids.get("Otto Rangel") + "/sell")).andExpect(status().isOk());
        play(aguias, delta).andExpect(status().isConflict());                    // perdeu o titular vendido
        play(delta, aguias).andExpect(status().isConflict());
        mvc.perform(patch("/api/v1/players/" + ids.get("Hugo Vaz") + "/condition").contentType(MediaType.APPLICATION_JSON)
                .content("{\"condition\": \"DOENTE\"}")).andExpect(status().isOk());
        play(delta, gama).andExpect(status().isConflict());                      // perdeu o titular DOENTE

        assertThat(count("partidas")).isZero();
        assertThat(count("duelos_partida")).isZero();
    }

    @Test
    void historyIsNewestFirstAndFiltersByClub() throws Exception {
        int first = JsonPath.read(play(aguias, delta).andReturn().getResponse().getContentAsString(), "$.id");
        int second = JsonPath.read(play(delta, aguias).andReturn().getResponse().getContentAsString(), "$.id");

        mvc.perform(get("/api/v1/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(contains(second, first)))
                .andExpect(jsonPath("$[0].home.club_name").value("Delta FC"))
                .andExpect(jsonPath("$[0].result").value("MANDANTE"))
                .andExpect(jsonPath("$[0].duels").doesNotExist());
        mvc.perform(get("/api/v1/matches").param("club_id", String.valueOf(aguias)))
                .andExpect(jsonPath("$[*].id").value(contains(second, first)));
        mvc.perform(get("/api/v1/matches").param("club_id", String.valueOf(gama)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        mvc.perform(get("/api/v1/matches").param("club_id", String.valueOf(gama + 1))).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/matches/" + (second + 1))).andExpect(status().isNotFound());
    }
}
