package br.com.footballmarket.league.api;

import br.com.footballmarket.club.domain.Formation;
import br.com.footballmarket.league.application.PlayedMatch;
import br.com.footballmarket.league.domain.DuelSide;
import br.com.footballmarket.league.domain.Match;
import br.com.footballmarket.league.domain.MatchDuel;
import br.com.footballmarket.league.domain.MatchResult;
import br.com.footballmarket.player.domain.Condition;
import br.com.footballmarket.player.domain.Position;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Partida (2.4.2). Em {@code GET /matches} vem sem {@code duels}.
 */
public record MatchResponse(
        Integer id,
        OffsetDateTime playedAt,
        TeamResponse home,
        TeamResponse away,
        MatchResult result,
        @JsonInclude(JsonInclude.Include.NON_NULL) List<DuelResponse> duels) {

    public record TeamResponse(Integer clubId, String clubName, String formation, int goals) {

        static TeamResponse of(Integer clubId, String clubName, Formation formation, int goals) {
            return new TeamResponse(clubId, clubName, formation.label(), goals);
        }
    }

    public record DuelResponse(int order, Integer attackingClubId, SideResponse attacker, SideResponse defender,
                               boolean goal) {

        static DuelResponse from(MatchDuel duel) {
            return new DuelResponse(duel.getOrder(), duel.getAttackingClubId(),
                    SideResponse.from(duel.attacker()), SideResponse.from(duel.defender()), duel.isGoal());
        }
    }

    public record SideResponse(Integer playerId, String playerName, Position position, Condition condition,
                               String attribute, int value) {

        static SideResponse from(DuelSide side) {
            return new SideResponse(side.playerId(), side.playerName(), side.position(), side.condition(),
                    side.attribute().label(), side.value());
        }
    }

    public static MatchResponse withDuels(PlayedMatch played) {
        return of(played, played.match().getDuels().stream().map(DuelResponse::from).toList());
    }

    public static MatchResponse summary(PlayedMatch played) {
        return of(played, null);
    }

    private static MatchResponse of(PlayedMatch played, List<DuelResponse> duels) {
        Match match = played.match();
        return new MatchResponse(
                match.getId(),
                match.getPlayedAt(),
                TeamResponse.of(match.getHomeClubId(), played.homeClubName(), match.getHomeFormation(), match.getHomeGoals()),
                TeamResponse.of(match.getAwayClubId(), played.awayClubName(), match.getAwayFormation(), match.getAwayGoals()),
                match.result(),
                duels);
    }
}
