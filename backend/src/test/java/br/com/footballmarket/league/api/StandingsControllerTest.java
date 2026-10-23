package br.com.footballmarket.league.api;

import br.com.footballmarket.club.application.ClubService;
import br.com.footballmarket.league.application.StandingsService;
import br.com.footballmarket.league.domain.Match;
import br.com.footballmarket.league.domain.MatchEngine;
import br.com.footballmarket.league.domain.MatchFixtures;
import br.com.footballmarket.league.infra.MatchRepository;
import br.com.footballmarket.shared.config.JacksonConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static br.com.footballmarket.club.domain.ClubFixtures.club;
import static br.com.footballmarket.player.domain.PlayerFixtures.NOW;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StandingsController.class)
@Import({StandingsService.class, JacksonConfig.class})
class StandingsControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ClubService clubs;

    @MockitoBean
    private MatchRepository matchRepository;

    /**
     * Exemplo 2.4.3: depois de Águias FC 1 × 2 Delta FC, com 3 clubes cadastrados.
     */
    @Test
    void standingsReproduceTheExampleOfTheSpec() throws Exception {
        when(clubs.findAllClubs()).thenReturn(List.of(
                club("Águias FC", "AGU", 1), club("Delta FC", "DFC", 2), club("Gama SC", "GSC", 3)));
        when(matchRepository.findAll()).thenReturn(List.of(Match.record(MatchFixtures.aguias(), MatchFixtures.delta(),
                MatchEngine.play(MatchFixtures.aguias(), MatchFixtures.delta()), NOW)));

        mvc.perform(get("/api/v1/standings"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [
                          {"position": 1, "club_id": 2, "club_name": "Delta FC", "played": 1, "wins": 1, "draws": 0,
                           "losses": 0, "goals_for": 2, "goals_against": 1, "goal_difference": 1, "points": 3},
                          {"position": 2, "club_id": 3, "club_name": "Gama SC", "played": 0, "wins": 0, "draws": 0,
                           "losses": 0, "goals_for": 0, "goals_against": 0, "goal_difference": 0, "points": 0},
                          {"position": 3, "club_id": 1, "club_name": "Águias FC", "played": 1, "wins": 0, "draws": 0,
                           "losses": 1, "goals_for": 1, "goals_against": 2, "goal_difference": -1, "points": 0}
                        ]
                        """, true));
    }

    @Test
    void leagueWithoutClubsIsAnEmptyArray() throws Exception {
        mvc.perform(get("/api/v1/standings"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]", true));
    }
}
