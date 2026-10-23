package br.com.footballmarket.club.domain;

import br.com.footballmarket.player.domain.Condition;
import br.com.footballmarket.player.domain.Player;
import br.com.footballmarket.player.domain.Position;
import br.com.footballmarket.shared.error.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static br.com.footballmarket.player.domain.PlayerFixtures.inClub;
import static br.com.footballmarket.player.domain.PlayerFixtures.player;
import static br.com.footballmarket.player.domain.PlayerFixtures.withCondition;
import static br.com.footballmarket.player.domain.PlayerFixtures.withId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LineupRulesTest {

    private static List<Position> positions(String spaced) {
        return Arrays.stream(spaced.split(" ")).map(Position::valueOf).toList();
    }

    /**
     * Exemplos da tabela de formações da RN19, em qualquer ordem.
     */
    @ParameterizedTest(name = "{0}: {1}")
    @CsvSource({
            "3-1, GOL ZAG LAT LAT ATA",
            "3-1, ATA ZAG ZAG ZAG GOL",
            "2-2, GOL ZAG LAT ATA ATA",
            "2-1-1, GOL ZAG LAT MEI ATA",
            "2-1-1, ATA MEI LAT LAT GOL",
            "1-2-1, GOL ZAG MEI MEI ATA",
            "1-2-1, GOL LAT MEI MEI ATA"
    })
    void formationsOfTheTable(String label, String spaced) {
        assertThat(Formation.fromLabel(label).orElseThrow().fits(positions(spaced))).isTrue();
    }

    @ParameterizedTest(name = "{0}: {1}")
    @CsvSource({
            "2-1-1, GOL GOL LAT MEI ATA",   // dois goleiros
            "2-1-1, ZAG ZAG LAT MEI ATA",   // sem goleiro
            "2-1-1, GOL ZAG LAT ATA ATA",   // meio vazio
            "3-1, GOL ZAG LAT MEI ATA",
            "2-2, GOL ZAG MEI ATA ATA",
            "1-2-1, GOL ZAG LAT MEI ATA",
            "2-1-1, GOL ZAG MEI ATA"        // só 4
    })
    void positionsThatDoNotFit(String label, String spaced) {
        assertThat(Formation.fromLabel(label).orElseThrow().fits(positions(spaced))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "2-1-1 ", "211", "4-0", "2–1–1", "TWO_ONE_ONE"})
    void onlyTheFourLabelsExist(String label) {
        assertThat(Formation.fromLabel(label)).isEmpty();
    }

    private static List<Player> squad(int clubId, Position... positions) {
        return IntStream.range(0, positions.length)
                .mapToObj(i -> withId(inClub(player("Titular " + (char) ('A' + i), positions[i], 25), clubId), 30 + i))
                .toList();
    }

    private static final Club AGUIAS = ClubFixtures.club("Águias FC", "AGU", 1);

    @Test
    void acceptsSquadPlayersWhoFitTheFormation() {
        List<Player> starters = squad(1, Position.GOL, Position.ZAG, Position.LAT, Position.MEI, Position.ATA);
        withCondition(starters.get(2), Condition.MAL_CONDICIONADO); // pode jogar, com o efeito da RN20

        assertThatCode(() -> LineupRules.requireValid(AGUIAS, Formation.TWO_ONE_ONE, starters))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsPlayerOutsideTheSquad() {
        List<Player> starters = new ArrayList<>(squad(1, Position.GOL, Position.ZAG, Position.LAT, Position.MEI));
        starters.add(withId(inClub(player("Outro Clube", Position.ATA, 25), 2), 40));

        assertThatThrownBy(() -> LineupRules.requireValid(AGUIAS, Formation.TWO_ONE_ONE, starters))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> LineupRules.requireValid(AGUIAS, Formation.TWO_ONE_ONE,
                List.of(withId(player("Jogador Livre", Position.GOL, 25), 41))))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rejectsSickPlayer() {
        List<Player> starters = squad(1, Position.GOL, Position.ZAG, Position.LAT, Position.MEI, Position.ATA);
        withCondition(starters.get(4), Condition.DOENTE);

        assertThatThrownBy(() -> LineupRules.requireValid(AGUIAS, Formation.TWO_ONE_ONE, starters))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("DOENTE");
    }

    @Test
    void rejectsPositionsThatDoNotFitTheFormation() {
        List<Player> starters = squad(1, Position.GOL, Position.ZAG, Position.LAT, Position.MEI, Position.ATA);

        assertThatThrownBy(() -> LineupRules.requireValid(AGUIAS, Formation.TWO_TWO, starters))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("2-2");
    }
}
