package br.com.footballmarket.player.api;

import br.com.footballmarket.player.application.PlayerService;
import br.com.footballmarket.player.domain.Condition;
import br.com.footballmarket.player.domain.Player;
import br.com.footballmarket.player.infra.PlayerRepository;
import br.com.footballmarket.shared.config.JacksonConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Optional;

import static br.com.footballmarket.player.domain.PlayerFixtures.NOW;
import static br.com.footballmarket.player.domain.PlayerFixtures.inClub;
import static br.com.footballmarket.player.domain.PlayerFixtures.meiaLua;
import static br.com.footballmarket.player.domain.PlayerFixtures.withId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Camada HTTP + serviço + domínio reais; só o repositório é simulado (o teste com banco real
 * fica em {@code PlayerApiIntegrationTest}).
 */
@WebMvcTest(PlayerController.class)
@Import({PlayerService.class, JacksonConfig.class, PlayerControllerTest.FixedClock.class})
class PlayerControllerTest {

    private static final String MEIA_LUA_JSON = """
            {"name": "  Meia   Lua ", "position": "MEI", "agility": 85, "age": 25,
             "pace": 70, "shooting": 68, "passing": 82, "dribbling": 79, "defending": 55, "physical": 64,
             "potential": 86, "base_value": 999, "overall": 1, "condition": "DOENTE", "club_id": 1}
            """;

    @TestConfiguration
    static class FixedClock {
        @Bean
        Clock clock() {
            return Clock.fixed(NOW.toInstant(), ZoneOffset.ofHours(-3));
        }
    }

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PlayerRepository repository;

    private ResultActions send(MockHttpServletRequestBuilder request,
                               String body) throws Exception {
        return mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private void givenStored(Player player) {
        when(repository.findByIdAndDeletedAtIsNull(player.getId())).thenReturn(Optional.of(player));
    }

    @Test
    void registerReturnsCreatedWithLocationAndCalculatedFields() throws Exception {
        when(repository.save(any(Player.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 2));

        send(post("/api/v1/players"), MEIA_LUA_JSON)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/players/2"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {"id": 2, "name": "Meia Lua", "position": "MEI", "agility": 85, "age": 25,
                         "condition": "APTO", "base_value": 40.00, "market_value": 40.00,
                         "pace": 70, "shooting": 68, "passing": 82, "dribbling": 79, "defending": 55,
                         "physical": 64, "potential": 86, "overall": 75, "club_id": null}
                        """, true));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"name\": \"Meia Lua\", \"position\": \"MEI\", \"agility\": 85, \"age\": 25}",           // sem indicadores
            "{\"name\": \"Meia Lua\", \"position\": \"mei\", \"agility\": 85, \"age\": 25, \"pace\": 70, \"shooting\": 68, \"passing\": 82, \"dribbling\": 79, \"defending\": 55, \"physical\": 64, \"potential\": 86}",
            "{\"name\": \"Meia Lua\", \"position\": \"MEI\", \"Agility\": 85, \"age\": 25, \"pace\": 70, \"shooting\": 68, \"passing\": 82, \"dribbling\": 79, \"defending\": 55, \"physical\": 64, \"potential\": 86}",
            "{\"name\": \"Meia Lua\", \"position\": \"MEI\", \"agility\": 101, \"age\": 25, \"pace\": 70, \"shooting\": 68, \"passing\": 82, \"dribbling\": 79, \"defending\": 55, \"physical\": 64, \"potential\": 86}",
            "{\"name\": \"Meia Lua\", \"position\": \"MEI\", \"agility\": 85, \"age\": 15, \"pace\": 70, \"shooting\": 68, \"passing\": 82, \"dribbling\": 79, \"defending\": 55, \"physical\": 64, \"potential\": 86}",
            "{\"name\": \"Meia Lua\", \"position\": \"MEI\", \"agility\": 85, \"age\": 25, \"pace\": 0, \"shooting\": 68, \"passing\": 82, \"dribbling\": 79, \"defending\": 55, \"physical\": 64, \"potential\": 86}",
            "{\"name\": \"Meia Lua\", \"position\": \"MEI\", \"agility\": 85, \"age\": 25, \"pace\": 70, \"shooting\": 68, \"passing\": 82, \"dribbling\": 79, \"defending\": 55, \"physical\": 64, \"potential\": 100}",
            "{\"name\": \"Meia Lua\", \"position\": \"MEI\", \"agility\": 85, \"age\": 25, \"pace\": 70, \"shooting\": 68, \"passing\": 82, \"dribbling\": 79, \"defending\": 55, \"physical\": 64, \"potential\": 80}",
            "{\"name\": \"Meia\", \"position\": \"MEI\", \"agility\": 85, \"age\": 25, \"pace\": 70, \"shooting\": 68, \"passing\": 82, \"dribbling\": 79, \"defending\": 55, \"physical\": 64, \"potential\": 86}"
    })
    void registerRejectsInvalidBodies(String body) throws Exception {
        send(post("/api/v1/players"), body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
        verify(repository, never()).save(any());
    }

    @Test
    void validationMessageNamesTheJsonField() throws Exception {
        send(post("/api/v1/players"), MEIA_LUA_JSON.replace("\"agility\": 85", "\"agility\": 101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O campo 'agility' deve estar entre 1 e 100."));
    }

    @Test
    void findByIdReturnsPlayer() throws Exception {
        givenStored(withId(meiaLua(), 2));

        mvc.perform(get("/api/v1/players/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.overall").value(75));
    }

    @Test
    void unknownOrDeletedPlayerIsNotFound() throws Exception {
        when(repository.findByIdAndDeletedAtIsNull(9)).thenReturn(Optional.empty());

        mvc.perform(get("/api/v1/players/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty());
        send(put("/api/v1/players/9"), "{\"name\": \"Davi Melo\", \"position\": \"MEI\", \"agility\": 70, \"age\": 23}")
                .andExpect(status().isNotFound());
        send(patch("/api/v1/players/9/condition"), "{\"condition\": \"APTO\"}")
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/players/9")).andExpect(status().isNotFound());
    }

    @Test
    void updateChangesEditableFieldsAndIgnoresTheRest() throws Exception {
        givenStored(withId(meiaLua(), 4));

        send(put("/api/v1/players/4"), """
                {"name": "Davi Melo", "position": "ATA", "agility": 70, "age": 26, "potential": 10, "pace": 1}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Davi Melo"))
                .andExpect(jsonPath("$.position").value("ATA"))
                .andExpect(jsonPath("$.potential").value(86))
                .andExpect(jsonPath("$.pace").value(70))
                .andExpect(jsonPath("$.base_value").value(40.00));
    }

    @Test
    void updateRequiresAllEditableFields() throws Exception {
        givenStored(withId(meiaLua(), 4));

        send(put("/api/v1/players/4"), "{\"name\": \"Davi Melo\", \"position\": \"MEI\", \"agility\": 70}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateRejectsBusinessRuleViolations() throws Exception {
        givenStored(withId(inClub(meiaLua(), 1), 4));

        send(put("/api/v1/players/4"), "{\"name\": \"Meia Lua\", \"position\": \"ATA\", \"agility\": 85, \"age\": 25}")
                .andExpect(status().isConflict());
        send(put("/api/v1/players/4"), "{\"name\": \"Meia Lua\", \"position\": \"MEI\", \"agility\": 85, \"age\": 24}")
                .andExpect(status().isConflict());
    }

    @Test
    void changeConditionRecalculatesMarketValue() throws Exception {
        givenStored(withId(meiaLua(), 2));

        send(patch("/api/v1/players/2/condition"), "{\"condition\": \"MAL_CONDICIONADO\", \"name\": \"ignorado\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.condition").value("MAL_CONDICIONADO"))
                .andExpect(jsonPath("$.market_value").value(37.00))
                .andExpect(jsonPath("$.name").value("Meia Lua"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"condition\": null}", "{\"condition\": \"apto\"}", "{\"condition\": \"CANSADO\"}"})
    void changeConditionRejectsInvalidValues(String body) throws Exception {
        givenStored(withId(meiaLua(), 2));

        send(patch("/api/v1/players/2/condition"), body).andExpect(status().isBadRequest());
    }

    @Test
    void sickPlayerCannotGoStraightToFit() throws Exception {
        Player player = withId(meiaLua(), 2);
        player.changeCondition(Condition.DOENTE, NOW);
        givenStored(player);

        send(patch("/api/v1/players/2/condition"), "{\"condition\": \"APTO\"}")
                .andExpect(status().isConflict());
    }

    @Test
    void deleteFreePlayerReturnsNoContent() throws Exception {
        Player player = withId(meiaLua(), 9);
        givenStored(player);

        mvc.perform(delete("/api/v1/players/9"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        assertThat(player.getDeletedAt()).isEqualTo(NOW);
    }

    @Test
    void deletePlayerInClubIsConflict() throws Exception {
        givenStored(withId(inClub(meiaLua(), 1), 9));

        mvc.perform(delete("/api/v1/players/9")).andExpect(status().isConflict());
    }
}
