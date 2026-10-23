package br.com.footballmarket.club.api;

import br.com.footballmarket.PostgresIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Clubes com banco real: mapeamento, unicidade da RN10 garantida pelo banco, limite de 20 e valores do elenco.
 */
class ClubApiIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void cleanDatabase() {
        jdbc.execute("TRUNCATE jogadores, clubes RESTART IDENTITY CASCADE");
    }

    private ResultActions createClub(String name, String acronym) throws Exception {
        return mvc.perform(post("/api/v1/clubs").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"" + name + "\", \"acronym\": \"" + acronym + "\"}"));
    }

    private int idOf(ResultActions result) throws Exception {
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id");
    }

    @Test
    void createPersistsAndAppliesRn10Uniqueness() throws Exception {
        int id = idOf(createClub("  Águias   FC ", "AGU").andExpect(status().isCreated()));

        Map<String, Object> row = jdbc.queryForMap("SELECT * FROM clubes WHERE id = ?", id);
        assertThat(row.get("nome")).isEqualTo("Águias FC");
        assertThat(row.get("sigla")).isEqualTo("AGU");
        assertThat((BigDecimal) row.get("caixa")).isEqualByComparingTo("300.00");
        assertThat(row.get("formacao")).isNull();

        createClub("ÁGUIAS fc", "AFC").andExpect(status().isConflict());
        createClub("Agulhas Unidas", "AGU").andExpect(status().isConflict());
        createClub("Aguias FC", "AFC").andExpect(status().isCreated());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM clubes", Integer.class)).isEqualTo(2);
    }

    @Test
    void databaseRejectsDuplicatesEvenWithoutTheApplicationChecks() {
        jdbc.update("INSERT INTO clubes (nome, nome_chave, sigla, criado_em) VALUES ('Águias FC', 'águias fc', 'AGU', ?)",
                OffsetDateTime.now());

        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO clubes (nome, nome_chave, sigla, criado_em) VALUES ('ÁGUIAS FC', 'águias fc', 'AFC', ?)",
                OffsetDateTime.now())).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO clubes (nome, nome_chave, sigla, criado_em) VALUES ('Agulhas FC', 'agulhas fc', 'AGU', ?)",
                OffsetDateTime.now())).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void leagueAcceptsAtMost20Clubs() throws Exception {
        for (int i = 0; i < 20; i++) {
            char letter = (char) ('A' + i);
            createClub("Clube " + letter + "Z", "C" + letter + "Z").andExpect(status().isCreated());
        }

        createClub("Clube UZ", "CUZ").andExpect(status().isConflict());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM clubes", Integer.class)).isEqualTo(20);
    }

    @Test
    void squadValueAndNetWorthAreCalculatedFromThePlayers() throws Exception {
        int aguias = idOf(createClub("Águias FC", "AGU"));
        int bravos = idOf(createClub("Bravos FC", "BFC"));
        String player = mvc.perform(post("/api/v1/players").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "Meia Lua", "position": "MEI", "agility": 85, "age": 25,
                         "pace": 70, "shooting": 68, "passing": 82, "dribbling": 79, "defending": 55,
                         "physical": 64, "potential": 86}
                        """))
                .andReturn().getResponse().getContentAsString();
        int playerId = JsonPath.read(player, "$.id");
        mvc.perform(patch("/api/v1/players/" + playerId + "/condition").contentType(MediaType.APPLICATION_JSON)
                .content("{\"condition\": \"MAL_CONDICIONADO\"}")).andExpect(status().isOk());
        // Enquanto a compra (Etapa 5) não existe, elenco e caixa são ajustados direto no banco.
        jdbc.update("UPDATE jogadores SET clube_id = ? WHERE id = ?", aguias, playerId);
        jdbc.update("UPDATE clubes SET caixa = 261.15 WHERE id = ?", aguias);

        mvc.perform(get("/api/v1/clubs/" + aguias))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cash").value(261.15))
                .andExpect(jsonPath("$.squad_value").value(37.00))
                .andExpect(jsonPath("$.net_worth").value(298.15))
                .andExpect(jsonPath("$.players[0].id").value(playerId))
                .andExpect(jsonPath("$.players[0].club_id").value(aguias));

        mvc.perform(get("/api/v1/clubs").param("sort", "net_worth_desc"))
                .andExpect(jsonPath("$[*].id").value(contains(bravos, aguias)));
        mvc.perform(get("/api/v1/clubs"))
                .andExpect(jsonPath("$[*].id").value(contains(aguias, bravos)));
        mvc.perform(get("/api/v1/clubs/" + (bravos + 1))).andExpect(status().isNotFound());
    }
}
