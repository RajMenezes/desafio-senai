package br.com.footballmarket.player.api;

import br.com.footballmarket.PostgresIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fluxo completo com banco real: valida o mapeamento JPA contra o schema do Flyway.
 */
class PlayerApiIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void cleanDatabase() {
        jdbc.execute("TRUNCATE jogadores, clubes RESTART IDENTITY CASCADE");
    }

    @Test
    void playerLifecycle() throws Exception {
        String created = mvc.perform(post("/api/v1/players").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "Meia Lua", "position": "MEI", "agility": 85, "age": 25,
                         "pace": 70, "shooting": 68, "passing": 82, "dribbling": 79, "defending": 55,
                         "physical": 64, "potential": 86}
                        """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int id = JsonPath.read(created, "$.id");

        Map<String, Object> row = jdbc.queryForMap("SELECT * FROM jogadores WHERE id = ?", id);
        assertThat(row.get("nome")).isEqualTo("Meia Lua");
        assertThat(row.get("condicao")).isEqualTo("APTO");
        assertThat((BigDecimal) row.get("valor_base")).isEqualByComparingTo("40.00");
        assertThat(row.get("clube_id")).isNull();

        mvc.perform(patch("/api/v1/players/" + id + "/condition").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"condition\": \"MAL_CONDICIONADO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.market_value").value(37.00));

        mvc.perform(put("/api/v1/players/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Meia Lua Nova\", \"position\": \"ATA\", \"agility\": 70, \"age\": 26}"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/players/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Meia Lua Nova"))
                .andExpect(jsonPath("$.position").value("ATA"))
                .andExpect(jsonPath("$.condition").value("MAL_CONDICIONADO"))
                .andExpect(jsonPath("$.base_value").value(40.00));

        mvc.perform(delete("/api/v1/players/" + id)).andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/players/" + id)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/players/" + id)).andExpect(status().isNotFound());
        assertThat(jdbc.queryForObject("SELECT excluido_em IS NOT NULL FROM jogadores WHERE id = ?", Boolean.class, id))
                .isTrue();
    }

    private int register(String name, String position, int age) throws Exception {
        String body = """
                {"name": "%s", "position": "%s", "agility": 70, "age": %d,
                 "pace": 70, "shooting": 68, "passing": 82, "dribbling": 79, "defending": 55,
                 "physical": 64, "potential": 86}
                """.formatted(name, position, age);
        String created = mvc.perform(post("/api/v1/players").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(created, "$.id");
    }

    @Test
    void listingFiltersInTheDatabase() throws Exception {
        int ana = register("Ana Goleira", "GOL", 20);
        int bia = register("Bia Zagueira", "ZAG", 28);
        int caio = register("Caio Atacante", "ATA", 30);
        int duda = register("Duda Excluída", "ATA", 30);
        mvc.perform(delete("/api/v1/players/" + duda)).andExpect(status().isNoContent());
        String club = mvc.perform(post("/api/v1/clubs").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Águias FC\", \"acronym\": \"AGU\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int clubId = JsonPath.read(club, "$.id");
        // Enquanto a compra (Etapa 5) não existe, o vínculo com o clube é feito direto no banco.
        jdbc.update("UPDATE jogadores SET clube_id = ? WHERE id = ?", clubId, bia);
        mvc.perform(patch("/api/v1/players/" + caio + "/condition").contentType(MediaType.APPLICATION_JSON)
                .content("{\"condition\": \"DOENTE\"}")).andExpect(status().isOk());

        mvc.perform(get("/api/v1/players"))
                .andExpect(jsonPath("$.data[*].id").value(contains(ana, bia, caio)))
                .andExpect(jsonPath("$.meta.total_items").value(3));
        mvc.perform(get("/api/v1/players").param("position", "ZAG"))
                .andExpect(jsonPath("$.data[*].id").value(contains(bia)));
        mvc.perform(get("/api/v1/players").param("condition", "DOENTE"))
                .andExpect(jsonPath("$.data[*].id").value(contains(caio)));
        mvc.perform(get("/api/v1/players").param("free", "true"))
                .andExpect(jsonPath("$.data[*].id").value(contains(ana, caio)));
        mvc.perform(get("/api/v1/players").param("free", "false"))
                .andExpect(jsonPath("$.data[*].id").value(contains(bia)));
        mvc.perform(get("/api/v1/players").param("club_id", String.valueOf(clubId)))
                .andExpect(jsonPath("$.data[*].id").value(contains(bia)));
        mvc.perform(get("/api/v1/players").param("club_id", String.valueOf(clubId + 1)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/players").param("min_age", "28").param("max_age", "29"))
                .andExpect(jsonPath("$.data[*].id").value(contains(bia)));
        mvc.perform(get("/api/v1/players").param("min_age", "28").param("sort", "age_desc"))
                .andExpect(jsonPath("$.data[*].id").value(contains(caio, bia)));
        mvc.perform(get("/api/v1/players").param("search", "ATA").param("free", "true"))
                .andExpect(jsonPath("$.data[*].id").value(contains(caio)));
    }
}
