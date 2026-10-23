package br.com.footballmarket.club.api;

import br.com.footballmarket.FixedClockConfig;
import br.com.footballmarket.club.application.ClubService;
import br.com.footballmarket.club.domain.Club;
import br.com.footballmarket.club.infra.ClubRepository;
import br.com.footballmarket.player.application.ClubDirectory;
import br.com.footballmarket.player.application.PlayerService;
import br.com.footballmarket.player.domain.Condition;
import br.com.footballmarket.player.domain.Player;
import br.com.footballmarket.player.domain.PlayerFixtures;
import br.com.footballmarket.player.domain.Position;
import br.com.footballmarket.player.infra.PlayerRepository;
import br.com.footballmarket.shared.config.JacksonConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Optional;

import static br.com.footballmarket.club.domain.ClubFixtures.aguias;
import static br.com.footballmarket.club.domain.ClubFixtures.club;
import static br.com.footballmarket.club.domain.ClubFixtures.withCash;
import static br.com.footballmarket.club.domain.ClubFixtures.withId;
import static br.com.footballmarket.player.domain.PlayerFixtures.inClub;
import static br.com.footballmarket.player.domain.PlayerFixtures.meiaLua;
import static br.com.footballmarket.player.domain.PlayerFixtures.player;
import static br.com.footballmarket.player.domain.PlayerFixtures.withCondition;
import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Camada HTTP + serviços + domínio reais; só os repositórios são simulados
 * (o teste com banco real fica em {@code ClubApiIntegrationTest}).
 */
@WebMvcTest(ClubController.class)
@Import({ClubService.class, PlayerService.class, JacksonConfig.class, FixedClockConfig.class})
class ClubControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ClubRepository clubRepository;

    @MockitoBean
    private PlayerRepository playerRepository;

    @MockitoBean
    private ClubDirectory clubDirectory;

    private ResultActions create(String body) throws Exception {
        return mvc.perform(post("/api/v1/clubs").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private static Player inSquad(Player player, int clubId, int id) {
        return PlayerFixtures.withId(inClub(player, clubId), id);
    }

    @Test
    void createReturnsCreatedWithLocationAndInitialValues() throws Exception {
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 1));

        create("{\"name\": \"  Águias   FC \", \"acronym\": \"AGU\", \"cash\": 1000, \"net_worth\": 1}")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/clubs/1"))
                .andExpect(content().json("""
                        {"id": 1, "name": "Águias FC", "acronym": "AGU",
                         "cash": 300.00, "squad_value": 0.00, "net_worth": 300.00}
                        """, true));
        verify(clubRepository).lockForCreation();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"acronym\": \"AGU\"}",
            "{\"name\": \"Águias FC\"}",
            "{\"name\": null, \"acronym\": \"AGU\"}",
            "{\"name\": \"   \", \"acronym\": \"AGU\"}",
            "{\"name\": \"Águias FC\", \"acronym\": 123}",
            "{\"name\": \"Águias FC\", \"acronym\": \"agu\"}",
            "{\"name\": \"Águias FC\", \"acronym\": \"GUI\"}",
            "{\"name\": \"Águias\", \"acronym\": \"AGU\"}",
            "{\"name\": \"Águias\\tFC\", \"acronym\": \"AGU\"}",
            "{\"Name\": \"Águias FC\", \"acronym\": \"AGU\"}",
            "[]"
    })
    void createRejectsInvalidBodies(String body) throws Exception {
        create(body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
        verify(clubRepository, never()).save(any());
    }

    @Test
    void duplicateNameIgnoringCaseIsConflict() throws Exception {
        when(clubRepository.existsByNameKey("águias fc")).thenReturn(true);

        create("{\"name\": \"  ÁGUIAS   fc \", \"acronym\": \"AGU\"}").andExpect(status().isConflict());
        verify(clubRepository, never()).save(any());
    }

    @Test
    void sameNameWithoutAccentIsAnotherName() throws Exception {
        when(clubRepository.existsByNameKey("águias fc")).thenReturn(true);
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 2));

        create("{\"name\": \"Aguias FC\", \"acronym\": \"AFC\"}").andExpect(status().isCreated());
    }

    @Test
    void duplicateAcronymIsConflict() throws Exception {
        when(clubRepository.existsByAcronym("AGU")).thenReturn(true);

        create("{\"name\": \"Agulhas Unidas\", \"acronym\": \"AGU\"}").andExpect(status().isConflict());
        verify(clubRepository, never()).save(any());
    }

    @Test
    void twentyFirstClubIsConflict() throws Exception {
        when(clubRepository.count()).thenReturn(20L);

        create("{\"name\": \"Águias FC\", \"acronym\": \"AGU\"}").andExpect(status().isConflict());
        verify(clubRepository, never()).save(any());
    }

    @Test
    void formatErrorComesBeforeConflicts() throws Exception {
        when(clubRepository.count()).thenReturn(20L);

        create("{\"name\": \"Águias FC\", \"acronym\": \"XYZ\"}").andExpect(status().isBadRequest());
    }

    @Test
    void listReturnsArrayWithCalculatedValuesAndNoSquad() throws Exception {
        Club aguias = withCash(withId(aguias(), 1), "261.15");
        Club bravos = club("Bravos FC", "BFC", 2);
        when(clubRepository.findAll()).thenReturn(List.of(bravos, aguias));
        when(playerRepository.findByClubIdIsNotNull())
                .thenReturn(List.of(inSquad(withCondition(meiaLua(), Condition.MAL_CONDICIONADO), 1, 2)));

        mvc.perform(get("/api/v1/clubs"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"id": 1, "name": "Águias FC", "acronym": "AGU",
                          "cash": 261.15, "squad_value": 37.00, "net_worth": 298.15},
                         {"id": 2, "name": "Bravos FC", "acronym": "BFC",
                          "cash": 300.00, "squad_value": 0.00, "net_worth": 300.00}]
                        """, true));
        mvc.perform(get("/api/v1/clubs").param("sort", "net_worth_desc"))
                .andExpect(jsonPath("$[*].id").value(contains(2, 1)));
        mvc.perform(get("/api/v1/clubs").param("sort", "net_worth_asc"))
                .andExpect(jsonPath("$[*].id").value(contains(1, 2)));
    }

    @Test
    void emptyLeagueIsAnEmptyArray() throws Exception {
        mvc.perform(get("/api/v1/clubs"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]", true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "NAME", "net_worth", "name,net_worth_asc", "value_desc"})
    void listRejectsInvalidSort(String sort) throws Exception {
        mvc.perform(get("/api/v1/clubs").param("sort", sort))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void detailBringsTheSquadInRn11Order() throws Exception {
        when(clubRepository.findById(1)).thenReturn(Optional.of(withCash(withId(aguias(), 1), "261.15")));
        when(playerRepository.findByClubId(1)).thenReturn(List.of(
                inSquad(player("Ana Ata", Position.ATA, 25), 1, 5),
                inSquad(withCondition(meiaLua(), Condition.MAL_CONDICIONADO), 1, 2),
                inSquad(player("Zeca Gol", Position.GOL, 25), 1, 7),
                inSquad(player("Bia Gol", Position.GOL, 25), 1, 9)));

        mvc.perform(get("/api/v1/clubs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Águias FC"))
                .andExpect(jsonPath("$.players[*].name").value(contains("Bia Gol", "Zeca Gol", "Meia Lua", "Ana Ata")))
                .andExpect(jsonPath("$.players[2].market_value").value(37.00))
                .andExpect(jsonPath("$.players[2].club_id").value(1))
                .andExpect(jsonPath("$.players[2].overall").value(75));
    }

    @Test
    void detailOfClubWithoutPlayers() throws Exception {
        when(clubRepository.findById(1)).thenReturn(Optional.of(withId(aguias(), 1)));

        mvc.perform(get("/api/v1/clubs/1"))
                .andExpect(content().json("""
                        {"id": 1, "name": "Águias FC", "acronym": "AGU",
                         "cash": 300.00, "squad_value": 0.00, "net_worth": 300.00, "players": []}
                        """, true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"9", "0", "01", "abc", "2147483648"})
    void unknownOrInvalidIdIsNotFound(String id) throws Exception {
        mvc.perform(get("/api/v1/clubs/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
