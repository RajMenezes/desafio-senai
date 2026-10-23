package br.com.footballmarket.player.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static br.com.footballmarket.player.domain.PlayerFixtures.player;
import static br.com.footballmarket.player.domain.PlayerFixtures.withCondition;
import static br.com.footballmarket.player.domain.PlayerFixtures.withId;
import static org.assertj.core.api.Assertions.assertThat;

class PlayerSortTest {

    private static List<String> sortedNames(List<PlayerSort> criteria, Player... players) {
        return Stream.of(players).sorted(PlayerSort.ordering(criteria)).map(Player::getName).toList();
    }

    @Test
    void acceptsOnlyExactParamValues() {
        assertThat(PlayerSort.fromParam("value_desc")).contains(PlayerSort.VALUE_DESC);
        assertThat(PlayerSort.fromParam("position")).contains(PlayerSort.POSITION);
    }

    @ParameterizedTest
    @ValueSource(strings = {"POSITION", "Name", "value", "VALUE_ASC", " name", ""})
    void rejectsOtherSpellings(String param) {
        assertThat(PlayerSort.fromParam(param)).isEmpty();
    }

    @Test
    void ascendingAndDescendingShareTheField() {
        assertThat(PlayerSort.VALUE_ASC.field()).isEqualTo(PlayerSort.VALUE_DESC.field());
        assertThat(PlayerSort.AGE_ASC.field()).isEqualTo(PlayerSort.AGE_DESC.field());
    }

    @Test
    void nameOrderIgnoresCaseAndAccents() {
        assertThat(sortedNames(PlayerSort.DEFAULT,
                withId(player("Bruno Dias", Position.MEI, 25), 1),
                withId(player("Zé Roberto", Position.MEI, 25), 2),
                withId(player("Álvaro Reis", Position.MEI, 25), 3),
                withId(player("alan Souza", Position.MEI, 25), 4)))
                .containsExactly("alan Souza", "Álvaro Reis", "Bruno Dias", "Zé Roberto");
    }

    @Test
    void sameNameIsBrokenByAscendingId() {
        Player second = withId(player("Davi Melo", Position.MEI, 25), 7);
        Player first = withId(player("Davi Melo", Position.ATA, 25), 3);

        assertThat(Stream.of(second, first).sorted(PlayerSort.ordering(PlayerSort.DEFAULT)).map(Player::getId))
                .containsExactly(3, 7);
    }

    @Test
    void namesEqualAfterFoldingAreBrokenByIdNotByAccent() {
        Player accented = withId(player("Júlio Reis", Position.MEI, 25), 2);
        Player plain = withId(player("julio reis", Position.MEI, 25), 5);

        assertThat(Stream.of(plain, accented).sorted(PlayerSort.ordering(PlayerSort.DEFAULT)).map(Player::getId))
                .containsExactly(2, 5);
    }

    @Test
    void squadOrderIsPositionThenNameThenId() {
        assertThat(Stream.of(
                        withId(player("Ana Ata", Position.ATA, 25), 1),
                        withId(player("Zeca Gol", Position.GOL, 25), 2),
                        withId(player("Bia Gol", Position.GOL, 25), 3))
                .sorted(PlayerSort.squadOrdering()).map(Player::getName))
                .containsExactly("Bia Gol", "Zeca Gol", "Ana Ata");
    }

    @Test
    void positionUsesFormationOrderNotAlphabetical() {
        assertThat(sortedNames(List.of(PlayerSort.POSITION),
                withId(player("Ana Ata", Position.ATA, 25), 1),
                withId(player("Bia Mei", Position.MEI, 25), 2),
                withId(player("Caio Lat", Position.LAT, 25), 3),
                withId(player("Duda Zag", Position.ZAG, 25), 4),
                withId(player("Eva Gol", Position.GOL, 25), 5)))
                .containsExactly("Eva Gol", "Duda Zag", "Caio Lat", "Bia Mei", "Ana Ata");
    }

    @Test
    void valueUsesMarketValueAndTiesFallBackToName() {
        Player fit = withId(player("Zeca Apto", Position.MEI, 25), 1);
        Player unfit = withId(withCondition(player("Bia Mal", Position.MEI, 25), Condition.MAL_CONDICIONADO), 2);
        Player alsoFit = withId(player("Ana Apta", Position.MEI, 25), 3);

        assertThat(sortedNames(List.of(PlayerSort.VALUE_DESC), fit, unfit, alsoFit))
                .containsExactly("Ana Apta", "Zeca Apto", "Bia Mal");
        assertThat(sortedNames(List.of(PlayerSort.VALUE_ASC), fit, unfit, alsoFit))
                .containsExactly("Bia Mal", "Ana Apta", "Zeca Apto");
    }

    @Test
    void secondCriterionBreaksTiesOfTheFirst() {
        Player oldGoalkeeper = withId(player("Ana Gol", Position.GOL, 30), 1);
        Player youngGoalkeeper = withId(player("Bia Gol", Position.GOL, 20), 2);
        Player striker = withId(player("Caio Ata", Position.ATA, 18), 3);

        assertThat(sortedNames(List.of(PlayerSort.POSITION, PlayerSort.AGE_DESC), striker, youngGoalkeeper, oldGoalkeeper))
                .containsExactly("Ana Gol", "Bia Gol", "Caio Ata");
        assertThat(sortedNames(List.of(PlayerSort.AGE_ASC), oldGoalkeeper, striker, youngGoalkeeper))
                .containsExactly("Caio Ata", "Bia Gol", "Ana Gol");
    }
}
