package br.com.footballmarket.league.domain;

import br.com.footballmarket.club.domain.Club;
import br.com.footballmarket.shared.text.TextRules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Classificação (RN21), calculada a cada consulta a partir dos placares; nunca gravada.
 */
public final class Standings {

    static final int POINTS_PER_WIN = 3;
    static final int POINTS_PER_DRAW = 1;

    /**
     * Mais pontos; maior saldo; mais gols marcados; nome do clube em ordem alfabética (Premissas).
     * Nomes iguais nessa comparação são desempatados pelo id, como nas outras ordenações por nome.
     */
    static final Comparator<Entry> ORDER = Comparator.comparingInt(Entry::points).reversed()
            .thenComparing(Comparator.comparingInt(Entry::goalDifference).reversed())
            .thenComparing(Comparator.comparingInt(Entry::goalsFor).reversed())
            .thenComparing(Entry::clubName, TextRules.ALPHABETICAL)
            .thenComparing(Entry::clubId);

    private Standings() {
    }

    /**
     * Placar de uma partida, o que basta para a classificação.
     */
    public record Score(Integer homeClubId, Integer awayClubId, int homeGoals, int awayGoals) {

        public static Score of(Match match) {
            return new Score(match.getHomeClubId(), match.getAwayClubId(), match.getHomeGoals(), match.getAwayGoals());
        }
    }

    /**
     * Uma linha da classificação. Saldo e pontos são derivados, nunca guardados.
     */
    public record Entry(int position, Integer clubId, String clubName, int played, int wins, int draws, int losses,
                        int goalsFor, int goalsAgainst) {

        public int goalDifference() {
            return goalsFor - goalsAgainst;
        }

        public int points() {
            return wins * POINTS_PER_WIN + draws * POINTS_PER_DRAW;
        }

        Entry at(int newPosition) {
            return new Entry(newPosition, clubId, clubName, played, wins, draws, losses, goalsFor, goalsAgainst);
        }
    }

    /**
     * Todos os clubes entram, inclusive os que ainda não jogaram (zerados). {@code position} vai de 1
     * ao total de clubes, sem repetir.
     */
    public static List<Entry> calculate(List<Club> clubs, List<Score> scores) {
        Map<Integer, Tally> tallies = new LinkedHashMap<>();
        clubs.forEach(club -> tallies.put(club.getId(), new Tally(club)));
        for (Score score : scores) {
            tallies.get(score.homeClubId()).add(score.homeGoals(), score.awayGoals());
            tallies.get(score.awayClubId()).add(score.awayGoals(), score.homeGoals());
        }
        List<Entry> sorted = tallies.values().stream().map(Tally::toEntry).sorted(ORDER).toList();
        List<Entry> ranked = new ArrayList<>(sorted.size());
        for (int i = 0; i < sorted.size(); i++) {
            ranked.add(sorted.get(i).at(i + 1));
        }
        return ranked;
    }

    private static final class Tally {

        private final Club club;
        private int wins;
        private int draws;
        private int losses;
        private int goalsFor;
        private int goalsAgainst;

        Tally(Club club) {
            this.club = club;
        }

        void add(int scored, int conceded) {
            goalsFor += scored;
            goalsAgainst += conceded;
            if (scored > conceded) {
                wins++;
            } else if (scored == conceded) {
                draws++;
            } else {
                losses++;
            }
        }

        Entry toEntry() {
            return new Entry(0, club.getId(), club.getName(), wins + draws + losses, wins, draws, losses,
                    goalsFor, goalsAgainst);
        }
    }
}
