package br.com.footballmarket.league.api;

import br.com.footballmarket.league.domain.Standings;

/**
 * Linha de {@code GET /standings} (2.4.3).
 */
public record StandingResponse(int position, Integer clubId, String clubName, int played, int wins, int draws,
                               int losses, int goalsFor, int goalsAgainst, int goalDifference, int points) {

    public static StandingResponse from(Standings.Entry entry) {
        return new StandingResponse(entry.position(), entry.clubId(), entry.clubName(), entry.played(), entry.wins(),
                entry.draws(), entry.losses(), entry.goalsFor(), entry.goalsAgainst(), entry.goalDifference(),
                entry.points());
    }
}
