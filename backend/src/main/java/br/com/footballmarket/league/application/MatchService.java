package br.com.footballmarket.league.application;

import br.com.footballmarket.club.application.ClubService;
import br.com.footballmarket.club.application.Lineup;
import br.com.footballmarket.club.application.LineupService;
import br.com.footballmarket.club.domain.Club;
import br.com.footballmarket.league.domain.Match;
import br.com.footballmarket.league.domain.MatchEngine;
import br.com.footballmarket.league.domain.MatchEngine.TeamSheet;
import br.com.footballmarket.league.infra.MatchRepository;
import br.com.footballmarket.shared.error.BusinessRuleException;
import br.com.footballmarket.shared.error.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Partidas (RN20) e o histórico delas (RN21). Jogar não muda o caixa, a escalação nem os jogadores.
 */
@Service
@Transactional
public class MatchService {

    private final ClubService clubs;
    private final LineupService lineups;
    private final MatchRepository matches;
    private final Clock clock;

    public MatchService(ClubService clubs, LineupService lineups, MatchRepository matches, Clock clock) {
        this.clubs = clubs;
        this.lineups = lineups;
        this.matches = matches;
        this.clock = clock;
    }

    /**
     * Conferências na ordem da RN20: mandante igual ao visitante (400, na entrada), clubes inexistentes (404)
     * e escalações incompletas (409); só então a partida é jogada e gravada.
     */
    public PlayedMatch play(Integer homeClubId, Integer awayClubId) {
        Club home = clubs.findClub(homeClubId);
        Club away = clubs.findClub(awayClubId);
        TeamSheet homeSheet = teamSheet(home, lineups.find(home.getId()));
        TeamSheet awaySheet = teamSheet(away, lineups.find(away.getId()));

        Match match = Match.record(homeSheet, awaySheet, MatchEngine.play(homeSheet, awaySheet), OffsetDateTime.now(clock));
        return new PlayedMatch(matches.save(match), home.getName(), away.getName());
    }

    @Transactional(readOnly = true)
    public PlayedMatch findById(Integer id) {
        Match match = matches.findWithDuelsById(id).orElseThrow(() -> new NotFoundException("Partida não encontrada."));
        Map<Integer, String> names = clubNames();
        return new PlayedMatch(match, names.get(match.getHomeClubId()), names.get(match.getAwayClubId()));
    }

    /**
     * @param clubId {@code null} lista todas; clube inexistente → 404
     */
    @Transactional(readOnly = true)
    public List<PlayedMatch> history(Integer clubId) {
        List<Match> found;
        if (clubId == null) {
            found = matches.findHistory();
        } else {
            clubs.requireExists(clubId);
            found = matches.findHistoryOfClub(clubId);
        }
        Map<Integer, String> names = clubNames();
        return found.stream()
                .map(match -> new PlayedMatch(match, names.get(match.getHomeClubId()), names.get(match.getAwayClubId())))
                .toList();
    }

    private static TeamSheet teamSheet(Club club, Lineup lineup) {
        if (!lineup.complete()) {
            throw new BusinessRuleException("O clube " + club.getName()
                    + " não tem a escalação completa, com os 5 titulares, e não pode jogar.");
        }
        return new TeamSheet(club.getId(), lineup.formation(), lineup.starters());
    }

    private Map<Integer, String> clubNames() {
        return clubs.findAllClubs().stream().collect(Collectors.toMap(Club::getId, Club::getName));
    }
}
