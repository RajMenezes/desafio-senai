package br.com.footballmarket.club.api;

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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Escalação com banco real: substituição, titular que sai na venda e ao ficar DOENTE, e a formação mantida.
 */
class LineupApiIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    private int club;
    private int gol;
    private int zag;
    private int lat;
    private int mei;
    private int ata;
    private int reserve;

    @BeforeEach
    void givenASquad() throws Exception {
        jdbc.execute("TRUNCATE escalacoes, transacoes, contratos, jogadores, clubes RESTART IDENTITY CASCADE");
        club = JsonPath.read(mvc.perform(post("/api/v1/clubs").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Águias FC\", \"acronym\": \"AGU\"}"))
                .andReturn().getResponse().getContentAsString(), "$.id");
        jdbc.update("UPDATE clubes SET caixa = 10000 WHERE id = ?", club);
        gol = signed("Rui Prado", "GOL");
        zag = signed("Téo Brandão", "ZAG");
        lat = signed("Davi Lins", "LAT");
        mei = signed("Lauro Neves", "MEI");
        ata = signed("Otto Rangel", "ATA");
        reserve = signed("Ivo Reserva", "ATA");
    }

    private int signed(String name, String position) throws Exception {
        int id = JsonPath.read(mvc.perform(post("/api/v1/players").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "%s", "position": "%s", "agility": 70, "age": 25,
                         "pace": 70, "shooting": 68, "passing": 82, "dribbling": 79, "defending": 55,
                         "physical": 64, "potential": 86}
                        """.formatted(name, position)))
                .andReturn().getResponse().getContentAsString(), "$.id");
        String loyalty = BrasiliaTime.format(OffsetDateTime.now(BrasiliaTime.OFFSET).truncatedTo(ChronoUnit.MINUTES)
                .plusDays(90)).substring(0, 16) + "-03:00";
        mvc.perform(post("/api/v1/players/" + id + "/buy").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"club_id\": " + club + ", \"loyalty_until\": \"" + loyalty + "\"}"))
                .andExpect(status().isOk());
        return id;
    }

    private ResultActions putLineup(String formation, int... ids) throws Exception {
        String list = String.join(", ", Arrays.stream(ids).mapToObj(String::valueOf).toList());
        return mvc.perform(put("/api/v1/clubs/" + club + "/lineup").contentType(MediaType.APPLICATION_JSON)
                .content("{\"formation\": \"" + formation + "\", \"player_ids\": [" + list + "]}"));
    }

    private List<Integer> startersInDatabase() {
        return jdbc.queryForList("SELECT jogador_id FROM escalacoes WHERE clube_id = ? ORDER BY jogador_id",
                Integer.class, club);
    }

    @Test
    void lineupLifecycle() throws Exception {
        BigDecimal cash = jdbc.queryForObject("SELECT caixa FROM clubes WHERE id = ?", BigDecimal.class, club);
        mvc.perform(get("/api/v1/clubs/" + club + "/lineup"))
                .andExpect(jsonPath("$.formation").isEmpty())
                .andExpect(jsonPath("$.complete").value(false))
                .andExpect(jsonPath("$.players").isEmpty());

        putLineup("2-1-1", ata, lat, gol, mei, zag)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.complete").value(true))
                .andExpect(jsonPath("$.players[*].id").value(contains(gol, zag, lat, mei, ata)));
        assertThat(startersInDatabase()).containsExactly(gol, zag, lat, mei, ata);
        assertThat(jdbc.queryForObject("SELECT formacao FROM clubes WHERE id = ?", String.class, club)).isEqualTo("2-1-1");

        // substitui a escalação inteira, repetindo 4 dos titulares
        putLineup("2-1-1", gol, zag, lat, mei, reserve).andExpect(status().isOk());
        assertThat(startersInDatabase()).containsExactly(gol, zag, lat, mei, reserve);
        // escalar não muda o caixa
        assertThat(jdbc.queryForObject("SELECT caixa FROM clubes WHERE id = ?", BigDecimal.class, club))
                .isEqualByComparingTo(cash);

        // titular DOENTE sai; a formação continua
        mvc.perform(patch("/api/v1/players/" + mei + "/condition").contentType(MediaType.APPLICATION_JSON)
                .content("{\"condition\": \"DOENTE\"}")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/clubs/" + club + "/lineup"))
                .andExpect(jsonPath("$.formation").value("2-1-1"))
                .andExpect(jsonPath("$.complete").value(false))
                .andExpect(jsonPath("$.players[*].id").value(containsInAnyOrder(gol, zag, lat, reserve)));

        // titular vendido sai
        mvc.perform(post("/api/v1/players/" + gol + "/sell")).andExpect(status().isOk());
        assertThat(startersInDatabase()).containsExactly(zag, lat, reserve);

        // quem deixa de estar DOENTE não volta sozinho
        mvc.perform(patch("/api/v1/players/" + mei + "/condition").contentType(MediaType.APPLICATION_JSON)
                .content("{\"condition\": \"MAL_CONDICIONADO\"}")).andExpect(status().isOk());
        assertThat(startersInDatabase()).containsExactly(zag, lat, reserve);
    }

    @Test
    void refusedLineupKeepsThePreviousOne() throws Exception {
        putLineup("2-1-1", gol, zag, lat, mei, ata).andExpect(status().isOk());
        mvc.perform(patch("/api/v1/players/" + reserve + "/condition").contentType(MediaType.APPLICATION_JSON)
                .content("{\"condition\": \"DOENTE\"}")).andExpect(status().isOk());

        putLineup("2-1-1", gol, zag, lat, mei, reserve).andExpect(status().isConflict());
        putLineup("1-2-1", gol, zag, lat, mei, ata).andExpect(status().isConflict());
        putLineup("2-1-1", gol, zag, lat, mei, 999).andExpect(status().isNotFound());

        assertThat(startersInDatabase()).containsExactly(gol, zag, lat, mei, ata);
        assertThat(jdbc.queryForObject("SELECT formacao FROM clubes WHERE id = ?", String.class, club)).isEqualTo("2-1-1");
    }

    @Test
    void playerOfAnotherClubIsConflict() throws Exception {
        mvc.perform(post("/api/v1/players/" + ata + "/sell")).andExpect(status().isOk());

        putLineup("2-1-1", gol, zag, lat, mei, ata).andExpect(status().isConflict());
        assertThat(startersInDatabase()).isEmpty();
    }
}
