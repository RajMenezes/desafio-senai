package br.com.footballmarket.club.api;

import br.com.footballmarket.FixedClockConfig;
import br.com.footballmarket.club.application.ClubService;
import br.com.footballmarket.club.application.LineupService;
import br.com.footballmarket.club.application.SquadContracts;
import br.com.footballmarket.club.domain.Club;
import br.com.footballmarket.club.domain.Formation;
import br.com.footballmarket.club.domain.LineupEntry;
import br.com.footballmarket.club.infra.ClubRepository;
import br.com.footballmarket.club.infra.LineupRepository;
import br.com.footballmarket.player.application.ClubDirectory;
import br.com.footballmarket.player.application.PlayerService;
import br.com.footballmarket.player.application.StarterRemoval;
import br.com.footballmarket.player.domain.Condition;
import br.com.footballmarket.player.domain.Player;
import br.com.footballmarket.player.domain.PlayerFixtures;
import br.com.footballmarket.player.domain.Position;
import br.com.footballmarket.player.infra.PlayerRepository;
import br.com.footballmarket.shared.config.JacksonConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static br.com.footballmarket.club.domain.ClubFixtures.aguias;
import static br.com.footballmarket.club.domain.ClubFixtures.withId;
import static br.com.footballmarket.player.domain.PlayerFixtures.NOW;
import static br.com.footballmarket.player.domain.PlayerFixtures.inClub;
import static br.com.footballmarket.player.domain.PlayerFixtures.player;
import static br.com.footballmarket.player.domain.PlayerFixtures.withCondition;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Escalação com HTTP, serviços e domínio reais; só os repositórios são simulados.
 * Usa os titulares do exemplo 2.4.1 (ids 30 a 34, no Águias FC).
 */
@WebMvcTest(LineupController.class)
@Import({LineupService.class, ClubService.class, PlayerService.class, JacksonConfig.class, FixedClockConfig.class})
class LineupControllerTest {

    private static final String EXAMPLE = "{\"formation\": \"2-1-1\", \"player_ids\": [34, 31, 30, 33, 32]}";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ClubRepository clubRepository;

    @MockitoBean
    private PlayerRepository playerRepository;

    @MockitoBean
    private LineupRepository lineupRepository;

    @MockitoBean
    private ClubDirectory clubDirectory;

    @MockitoBean
    private StarterRemoval starterRemoval;

    @MockitoBean
    private SquadContracts squadContracts;

    private Club aguias;
    private Map<Integer, Player> players;

    private static Player starter(int id, String name, Position position) {
        return PlayerFixtures.withId(inClub(player(name, position, 25), 1), id);
    }

    @BeforeEach
    void givenTheExampleSquad() {
        aguias = withId(aguias(), 1);
        players = Map.of(
                30, starter(30, "Rui Prado", Position.GOL),
                31, withCondition(starter(31, "Davi Lins", Position.LAT), Condition.MAL_CONDICIONADO),
                32, starter(32, "Téo Brandão", Position.ZAG),
                33, starter(33, "Lauro Neves", Position.MEI),
                34, starter(34, "Otto Rangel", Position.ATA),
                35, starter(35, "Ivo Reserva", Position.ATA));
        when(clubRepository.existsById(1)).thenReturn(true);
        when(clubRepository.findById(1)).thenReturn(Optional.of(aguias));
        when(clubRepository.findByIdForUpdate(1)).thenReturn(Optional.of(aguias));
        players.forEach((id, player) -> when(playerRepository.findActiveForUpdate(id)).thenReturn(Optional.of(player)));
    }

    private ResultActions putLineup(int clubId, String body) throws Exception {
        return mvc.perform(put("/api/v1/clubs/" + clubId + "/lineup").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private void verifyNothingSaved() {
        verify(lineupRepository, never()).deleteAllOfClub(anyInt());
        verify(lineupRepository, never()).saveAll(anyList());
        assertThat(aguias.getFormation()).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void replaceReproducesTheExampleInSquadOrder() throws Exception {
        putLineup(1, EXAMPLE)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.club_id").value(1))
                .andExpect(jsonPath("$.formation").value("2-1-1"))
                .andExpect(jsonPath("$.complete").value(true))
                .andExpect(jsonPath("$.players[*].id").value(contains(30, 32, 31, 33, 34)))
                .andExpect(jsonPath("$.players[*].position").value(contains("GOL", "ZAG", "LAT", "MEI", "ATA")))
                .andExpect(jsonPath("$.players[2].condition").value("MAL_CONDICIONADO"))
                .andExpect(jsonPath("$.players[0].market_value").isNumber())
                .andExpect(jsonPath("$.players[0].overall").isNumber());

        assertThat(aguias.getFormation()).isEqualTo(Formation.TWO_ONE_ONE);
        ArgumentCaptor<List<LineupEntry>> saved = ArgumentCaptor.forClass(List.class);
        InOrder order = inOrder(lineupRepository);
        order.verify(lineupRepository).deleteAllOfClub(1);
        order.verify(lineupRepository).saveAll(saved.capture());
        assertThat(saved.getValue()).extracting(LineupEntry::getPlayerId).containsExactlyInAnyOrder(30, 31, 32, 33, 34);
        assertThat(saved.getValue()).allSatisfy(entry -> {
            assertThat(entry.getClubId()).isEqualTo(1);
            assertThat(entry.getCreatedAt()).isEqualTo(NOW);
        });
    }

    @Test
    void playersAreLockedInAscendingIdOrder() throws Exception {
        putLineup(1, EXAMPLE).andExpect(status().isOk());

        InOrder order = inOrder(playerRepository, clubRepository);
        for (int id = 30; id <= 34; id++) {
            order.verify(playerRepository).findActiveForUpdate(id);
        }
        order.verify(clubRepository).findByIdForUpdate(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"player_ids\": [30, 31, 32, 33, 34]}",
            "{\"formation\": null, \"player_ids\": [30, 31, 32, 33, 34]}",
            "{\"formation\": \"4-0\", \"player_ids\": [30, 31, 32, 33, 34]}",
            "{\"formation\": \"2-1-1 \", \"player_ids\": [30, 31, 32, 33, 34]}",
            "{\"formation\": 211, \"player_ids\": [30, 31, 32, 33, 34]}",
            "{\"formation\": \"2-1-1\"}",
            "{\"formation\": \"2-1-1\", \"player_ids\": null}",
            "{\"formation\": \"2-1-1\", \"player_ids\": 30}",
            "{\"formation\": \"2-1-1\", \"player_ids\": \"30,31,32,33,34\"}",
            "{\"formation\": \"2-1-1\", \"player_ids\": [30, 31, 32, 33]}",
            "{\"formation\": \"2-1-1\", \"player_ids\": [30, 31, 32, 33, 34, 35]}",
            "{\"formation\": \"2-1-1\", \"player_ids\": []}",
            "{\"formation\": \"2-1-1\", \"player_ids\": [30, 31, 32, 33, 33]}",
            "{\"formation\": \"2-1-1\", \"player_ids\": [30, 31, 32, 33, 0]}",
            "{\"formation\": \"2-1-1\", \"player_ids\": [30, 31, 32, 33, -34]}",
            "{\"formation\": \"2-1-1\", \"player_ids\": [30, 31, 32, 33, null]}",
            "{\"formation\": \"2-1-1\", \"player_ids\": [30, 31, 32, 33, 34.5]}",
            "{\"formation\": \"2-1-1\", \"player_ids\": [30, 31, 32, 33, \"34\"]}",
            "{\"formation\": \"2-1-1\", \"playerIds\": [30, 31, 32, 33, 34]}"
    })
    void replaceRejectsInvalidBodies(String body) throws Exception {
        putLineup(1, body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
        verifyNothingSaved();
    }

    @Test
    void integralFloatIdIsAccepted() throws Exception {
        putLineup(1, "{\"formation\": \"2-1-1\", \"player_ids\": [34.0, 31, 30, 33, 32]}").andExpect(status().isOk());
    }

    @Test
    void unknownClubOrPlayerIsNotFound() throws Exception {
        putLineup(7, EXAMPLE).andExpect(status().isNotFound()).andExpect(jsonPath("$.message").isNotEmpty());
        putLineup(1, "{\"formation\": \"2-1-1\", \"player_ids\": [34, 31, 30, 33, 99]}").andExpect(status().isNotFound());
        putLineup(0, EXAMPLE).andExpect(status().isNotFound());
        verifyNothingSaved();
    }

    @Test
    void formatErrorComesBeforeNotFound() throws Exception {
        putLineup(7, "{\"formation\": \"4-0\", \"player_ids\": [30, 31, 32, 33, 34]}").andExpect(status().isBadRequest());
    }

    @Test
    void playerOutsideTheSquadIsConflict() throws Exception {
        inClub(players.get(34), 2);

        putLineup(1, EXAMPLE).andExpect(status().isConflict()).andExpect(jsonPath("$.message").isNotEmpty());
        verifyNothingSaved();
    }

    @Test
    void sickPlayerIsConflict() throws Exception {
        withCondition(players.get(33), Condition.DOENTE);

        putLineup(1, EXAMPLE).andExpect(status().isConflict());
        verifyNothingSaved();
    }

    @Test
    void positionsThatDoNotFitAreConflict() throws Exception {
        putLineup(1, "{\"formation\": \"2-2\", \"player_ids\": [34, 31, 30, 33, 32]}").andExpect(status().isConflict());
        putLineup(1, "{\"formation\": \"2-1-1\", \"player_ids\": [34, 31, 30, 35, 32]}").andExpect(status().isConflict());
        verifyNothingSaved();
    }

    @Test
    void clubWithoutLineup() throws Exception {
        mvc.perform(get("/api/v1/clubs/1/lineup"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"club_id": 1, "formation": null, "complete": false, "players": []}
                        """, true));
    }

    @Test
    void incompleteLineupKeepsTheFormation() throws Exception {
        aguias.changeFormation(Formation.TWO_ONE_ONE);
        List<LineupEntry> remaining = List.of(30, 31, 32, 33).stream().map(id -> new LineupEntry(1, id, NOW)).toList();
        when(lineupRepository.findByClubId(1)).thenReturn(remaining);
        when(playerRepository.findAllById(any())).thenReturn(List.of(players.get(33), players.get(30), players.get(31), players.get(32)));

        mvc.perform(get("/api/v1/clubs/1/lineup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formation").value("2-1-1"))
                .andExpect(jsonPath("$.complete").value(false))
                .andExpect(jsonPath("$.players[*].id").value(contains(30, 32, 31, 33)));
    }

    @Test
    void lineupOfUnknownClubIsNotFound() throws Exception {
        mvc.perform(get("/api/v1/clubs/7/lineup")).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/clubs/abc/lineup")).andExpect(status().isNotFound());
    }
}
