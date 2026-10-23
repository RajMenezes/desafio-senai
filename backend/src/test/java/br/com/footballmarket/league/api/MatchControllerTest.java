package br.com.footballmarket.league.api;

import br.com.footballmarket.club.application.ClubService;
import br.com.footballmarket.club.application.Lineup;
import br.com.footballmarket.club.application.LineupService;
import br.com.footballmarket.club.domain.Club;
import br.com.footballmarket.club.domain.Formation;
import br.com.footballmarket.league.application.MatchService;
import br.com.footballmarket.league.domain.Match;
import br.com.footballmarket.league.domain.MatchEngine;
import br.com.footballmarket.league.domain.MatchFixtures;
import br.com.footballmarket.league.infra.MatchRepository;
import br.com.footballmarket.shared.config.JacksonConfig;
import br.com.footballmarket.shared.error.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static br.com.footballmarket.club.domain.ClubFixtures.club;
import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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
 * Partidas com HTTP, serviço e motor reais; clubes, escalações e o repositório de partidas são simulados.
 * O relógio está no momento do exemplo 2.4.2 (12/11/2026, 15h).
 */
@WebMvcTest(MatchController.class)
@Import({MatchService.class, JacksonConfig.class, MatchControllerTest.MatchClock.class})
class MatchControllerTest {

    static final OffsetDateTime PLAYED_AT = OffsetDateTime.of(2026, 11, 12, 15, 0, 0, 0, ZoneOffset.ofHours(-3));
    private static final String AGUIAS_X_DELTA = "{\"home_club_id\": 1, \"away_club_id\": 2}";

    @TestConfiguration
    static class MatchClock {
        @Bean
        Clock clock() {
            return Clock.fixed(PLAYED_AT.toInstant(), ZoneOffset.ofHours(-3));
        }
    }

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ClubService clubs;

    @MockitoBean
    private LineupService lineups;

    @MockitoBean
    private MatchRepository matchRepository;

    private final Club aguias = club("Águias FC", "AGU", 1);
    private final Club delta = club("Delta FC", "DFC", 2);

    @BeforeEach
    void givenTheExampleClubs() {
        when(clubs.findClub(1)).thenReturn(aguias);
        when(clubs.findClub(2)).thenReturn(delta);
        when(clubs.findClub(7)).thenThrow(new NotFoundException("Clube não encontrado."));
        when(clubs.findAllClubs()).thenReturn(List.of(aguias, delta));
        when(lineups.find(1)).thenReturn(new Lineup(1, Formation.TWO_ONE_ONE, MatchFixtures.aguias().starters()));
        when(lineups.find(2)).thenReturn(new Lineup(2, Formation.TWO_ONE_ONE, MatchFixtures.delta().starters()));
        when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 1));
    }

    private static Match withId(Match match, int id) {
        ReflectionTestUtils.setField(match, "id", id);
        return match;
    }

    private ResultActions play(String body) throws Exception {
        return mvc.perform(post("/api/v1/matches").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void playReproducesTheExampleOfTheSpec() throws Exception {
        play(AGUIAS_X_DELTA)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/matches/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.played_at").value("2026-11-12T15:00:00-03:00"))
                .andExpect(content().json("""
                        {"home": {"club_id": 1, "club_name": "Águias FC", "formation": "2-1-1", "goals": 1},
                         "away": {"club_id": 2, "club_name": "Delta FC", "formation": "2-1-1", "goals": 2},
                         "result": "VISITANTE"}
                        """))
                .andExpect(jsonPath("$.duels.length()").value(6))
                .andExpect(content().json("""
                        {"duels": [
                          {"order": 1, "attacking_club_id": 1,
                           "attacker": {"player_id": 34, "player_name": "Otto Rangel", "position": "ATA", "condition": "APTO",
                                        "attribute": "shooting", "value": 75},
                           "defender": {"player_id": 40, "player_name": "Ivo Teles", "position": "GOL", "condition": "APTO",
                                        "attribute": "defending", "value": 72},
                           "goal": true},
                          {"order": 2, "attacking_club_id": 1,
                           "attacker": {"player_id": 33, "player_name": "Lauro Neves", "position": "MEI", "condition": "APTO",
                                        "attribute": "passing", "value": 82},
                           "defender": {"player_id": 41, "player_name": "Hugo Vaz", "position": "ZAG", "condition": "APTO",
                                        "attribute": "defending", "value": 84},
                           "goal": false},
                          {"order": 3, "attacker": {"player_id": 31, "condition": "MAL_CONDICIONADO", "attribute": "dribbling", "value": 63},
                           "defender": {"player_id": 42, "value": 63}, "goal": false},
                          {"order": 4, "attacking_club_id": 2, "goal": true},
                          {"order": 5, "goal": false},
                          {"order": 6, "attacker": {"player_id": 42, "value": 79},
                           "defender": {"player_id": 31, "condition": "MAL_CONDICIONADO", "value": 56}, "goal": true}]}
                        """));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"away_club_id\": 2}",
            "{\"home_club_id\": 1}",
            "{\"home_club_id\": null, \"away_club_id\": 2}",
            "{\"home_club_id\": 0, \"away_club_id\": 2}",
            "{\"home_club_id\": 1, \"away_club_id\": -2}",
            "{\"home_club_id\": \"1\", \"away_club_id\": 2}",
            "{\"home_club_id\": 1.5, \"away_club_id\": 2}",
            "{\"home_club_id\": 1, \"away_club_id\": 1}",
            "{\"homeClubId\": 1, \"awayClubId\": 2}",
            "[]"
    })
    void playRejectsInvalidBodies(String body) throws Exception {
        play(body).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").isNotEmpty());
        verify(matchRepository, never()).save(any());
    }

    @Test
    void sameClubIsBadRequestEvenIfItDoesNotExist() throws Exception {
        play("{\"home_club_id\": 7, \"away_club_id\": 7}").andExpect(status().isBadRequest());
    }

    @Test
    void unknownClubIsNotFound() throws Exception {
        play("{\"home_club_id\": 7, \"away_club_id\": 2}").andExpect(status().isNotFound());
        play("{\"home_club_id\": 1, \"away_club_id\": 7}").andExpect(status().isNotFound());
        verify(matchRepository, never()).save(any());
    }

    @Test
    void unknownClubComesBeforeIncompleteLineup() throws Exception {
        when(lineups.find(1)).thenReturn(new Lineup(1, null, List.of()));

        play("{\"home_club_id\": 1, \"away_club_id\": 7}").andExpect(status().isNotFound());
    }

    @Test
    void clubThatNeverLinedUpIsConflict() throws Exception {
        when(lineups.find(2)).thenReturn(new Lineup(2, null, List.of()));

        play(AGUIAS_X_DELTA).andExpect(status().isConflict()).andExpect(jsonPath("$.message").isNotEmpty());
        verify(matchRepository, never()).save(any());
    }

    @Test
    void incompleteLineupIsConflict() throws Exception {
        when(lineups.find(1)).thenReturn(new Lineup(1, Formation.TWO_ONE_ONE,
                MatchFixtures.aguias().starters().subList(0, 4)));

        play(AGUIAS_X_DELTA).andExpect(status().isConflict());
        verify(matchRepository, never()).save(any());
    }

    private Match recorded(int id, OffsetDateTime playedAt) {
        Match match = Match.record(MatchFixtures.aguias(), MatchFixtures.delta(),
                MatchEngine.play(MatchFixtures.aguias(), MatchFixtures.delta()), playedAt);
        return withId(match, id);
    }

    @Test
    void historyHasNoDuels() throws Exception {
        when(matchRepository.findHistory()).thenReturn(List.of(recorded(2, PLAYED_AT.plusHours(1)), recorded(1, PLAYED_AT)));

        mvc.perform(get("/api/v1/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(contains(2, 1)))
                .andExpect(jsonPath("$[0].home.club_name").value("Águias FC"))
                .andExpect(jsonPath("$[0].away.goals").value(2))
                .andExpect(jsonPath("$[0].result").value("VISITANTE"))
                .andExpect(jsonPath("$[0].duels").doesNotExist());
    }

    @Test
    void historyFilteredByClub() throws Exception {
        when(matchRepository.findHistoryOfClub(2)).thenReturn(List.of(recorded(1, PLAYED_AT)));

        mvc.perform(get("/api/v1/matches").param("club_id", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(contains(1)));
        verify(clubs).requireExists(2);
    }

    @Test
    void historyOfUnknownClubIsNotFound() throws Exception {
        doThrow(new NotFoundException("Clube não encontrado.")).when(clubs).requireExists(7);

        mvc.perform(get("/api/v1/matches").param("club_id", "7")).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "0", "01", "abc", "-1"})
    void historyRejectsInvalidClubFilter(String clubId) throws Exception {
        mvc.perform(get("/api/v1/matches").param("club_id", clubId)).andExpect(status().isBadRequest());
    }

    @Test
    void emptyHistoryIsAnEmptyArray() throws Exception {
        mvc.perform(get("/api/v1/matches")).andExpect(status().isOk()).andExpect(content().json("[]", true));
    }

    @Test
    void matchDetailHasTheSixDuels() throws Exception {
        when(matchRepository.findWithDuelsById(1)).thenReturn(Optional.of(recorded(1, PLAYED_AT)));

        mvc.perform(get("/api/v1/matches/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duels[*].order").value(contains(1, 2, 3, 4, 5, 6)))
                .andExpect(jsonPath("$.duels[*].goal").value(contains(true, false, false, true, false, true)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"9", "0", "abc"})
    void unknownMatchIsNotFound(String id) throws Exception {
        mvc.perform(get("/api/v1/matches/" + id)).andExpect(status().isNotFound());
    }
}
