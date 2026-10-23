package br.com.footballmarket.league.application;

import br.com.footballmarket.club.application.ClubService;
import br.com.footballmarket.league.domain.Standings;
import br.com.footballmarket.league.infra.MatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Classificação com todos os clubes (RN21), calculada a cada consulta.
 */
@Service
@Transactional(readOnly = true)
public class StandingsService {

    private final ClubService clubs;
    private final MatchRepository matches;

    public StandingsService(ClubService clubs, MatchRepository matches) {
        this.clubs = clubs;
        this.matches = matches;
    }

    public List<Standings.Entry> standings() {
        List<Standings.Score> scores = matches.findAll().stream().map(Standings.Score::of).toList();
        return Standings.calculate(clubs.findAllClubs(), scores);
    }
}
