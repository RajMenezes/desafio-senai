package br.com.footballmarket.league.domain;

import br.com.footballmarket.club.domain.Club;
import br.com.footballmarket.league.domain.Standings.Entry;
import br.com.footballmarket.league.domain.Standings.Score;
import org.junit.jupiter.api.Test;

import java.util.List;

import static br.com.footballmarket.club.domain.ClubFixtures.club;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class StandingsTest {

    private static final Club AGUIAS = club("Águias FC", "AGU", 1);
    private static final Club DELTA = club("Delta FC", "DFC", 2);
    private static final Club GAMA = club("Gama SC", "GSC", 3);
    private static final Club EPSILON = club("Épsilon EC", "EEC", 4);
    private static final Club MANGUE = club("Mangue FC", "MFC", 5);
    private static final Club TUBAROES = club("Tubarões EC", "TEC", 6);

    /**
     * Exemplo da RN21: 6 clubes e 4 partidas; Mangue FC e Tubarões EC ainda não jogaram.
     */
    @Test
    void reproducesTheExampleOfTheSpec() {
        List<Entry> standings = Standings.calculate(List.of(AGUIAS, DELTA, GAMA, EPSILON, MANGUE, TUBAROES), List.of(
                new Score(1, 2, 1, 2),   // Águias FC 1 × 2 Delta FC
                new Score(3, 4, 2, 1),   // Gama SC 2 × 1 Épsilon EC
                new Score(4, 1, 3, 2),   // Épsilon EC 3 × 2 Águias FC
                new Score(2, 3, 0, 1))); // Delta FC 0 × 1 Gama SC

        assertThat(standings)
                .extracting(Entry::position, Entry::clubName, Entry::points, Entry::goalDifference, Entry::goalsFor)
                .containsExactly(
                        tuple(1, "Gama SC", 6, 2, 3),
                        tuple(2, "Épsilon EC", 3, 0, 4),
                        tuple(3, "Delta FC", 3, 0, 2),
                        tuple(4, "Mangue FC", 0, 0, 0),
                        tuple(5, "Tubarões EC", 0, 0, 0),
                        tuple(6, "Águias FC", 0, -2, 3));
    }

    /**
     * Exemplo 2.4.3: 3 clubes, depois de Águias FC 1 × 2 Delta FC.
     */
    @Test
    void countsPlayedWinsDrawsAndLosses() {
        List<Entry> standings = Standings.calculate(List.of(AGUIAS, DELTA, GAMA), List.of(new Score(1, 2, 1, 2)));

        assertThat(standings).containsExactly(
                new Entry(1, 2, "Delta FC", 1, 1, 0, 0, 2, 1),
                new Entry(2, 3, "Gama SC", 0, 0, 0, 0, 0, 0),
                new Entry(3, 1, "Águias FC", 1, 0, 0, 1, 1, 2));
        assertThat(standings.getFirst().points()).isEqualTo(3);
        assertThat(standings.getLast().goalDifference()).isEqualTo(-1);
    }

    @Test
    void drawIsWorthOnePointForEach() {
        List<Entry> standings = Standings.calculate(List.of(AGUIAS, DELTA),
                List.of(new Score(1, 2, 2, 2), new Score(2, 1, 0, 0)));

        assertThat(standings).allSatisfy(entry -> {
            assertThat(entry.played()).isEqualTo(2);
            assertThat(entry.draws()).isEqualTo(2);
            assertThat(entry.points()).isEqualTo(2);
        });
        assertThat(standings).extracting(Entry::clubName).containsExactly("Águias FC", "Delta FC");
    }

    @Test
    void repeatedMatchesAddUp() {
        List<Entry> standings = Standings.calculate(List.of(AGUIAS, DELTA),
                List.of(new Score(1, 2, 3, 0), new Score(1, 2, 3, 0), new Score(2, 1, 1, 0)));

        assertThat(standings.getFirst()).isEqualTo(new Entry(1, 1, "Águias FC", 3, 2, 0, 1, 6, 1));
    }

    @Test
    void namesEqualAfterFoldingAreBrokenByIdAndPositionsNeverRepeat() {
        Club plain = club("Aguias FC", "AFC", 9);

        List<Entry> standings = Standings.calculate(List.of(plain, AGUIAS), List.of());

        assertThat(standings).extracting(Entry::position, Entry::clubId).containsExactly(tuple(1, 1), tuple(2, 9));
    }

    @Test
    void emptyLeagueHasNoRows() {
        assertThat(Standings.calculate(List.of(), List.of())).isEmpty();
    }
}
