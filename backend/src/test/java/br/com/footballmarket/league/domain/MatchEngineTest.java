package br.com.footballmarket.league.domain;

import br.com.footballmarket.club.domain.Formation;
import br.com.footballmarket.league.domain.MatchEngine.MatchOutcome;
import br.com.footballmarket.player.domain.Condition;
import br.com.footballmarket.player.domain.Player;
import br.com.footballmarket.player.domain.Position;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static br.com.footballmarket.league.domain.MatchFixtures.aguias;
import static br.com.footballmarket.league.domain.MatchFixtures.delta;
import static br.com.footballmarket.league.domain.MatchFixtures.sheet;
import static br.com.footballmarket.league.domain.MatchFixtures.starter;
import static br.com.footballmarket.player.domain.PlayerFixtures.withCondition;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class MatchEngineTest {

    /**
     * Exemplo completo da RN20: Águias FC 1 × 2 Delta FC.
     */
    @Test
    void reproducesTheExampleOfTheSpec() {
        MatchOutcome outcome = MatchEngine.play(aguias(), delta());

        assertThat(outcome.duels())
                .extracting(DuelOutcome::order, DuelOutcome::attackingClubId,
                        duel -> duel.attacker().playerName(), duel -> duel.attacker().attribute(), duel -> duel.attacker().value(),
                        duel -> duel.defender().playerName(), duel -> duel.defender().value(), DuelOutcome::goal)
                .containsExactly(
                        tuple(1, 1, "Otto Rangel", DuelAttribute.SHOOTING, 75, "Ivo Teles", 72, true),
                        tuple(2, 1, "Lauro Neves", DuelAttribute.PASSING, 82, "Hugo Vaz", 84, false),
                        tuple(3, 1, "Davi Lins", DuelAttribute.DRIBBLING, 63, "Lino Matos", 63, false),
                        tuple(4, 2, "Ugo Falcão", DuelAttribute.SHOOTING, 81, "Rui Prado", 77, true),
                        tuple(5, 2, "Nei Soares", DuelAttribute.PASSING, 70, "Téo Brandão", 84, false),
                        tuple(6, 2, "Lino Matos", DuelAttribute.DRIBBLING, 79, "Davi Lins", 56, true));
        assertThat(outcome.homeGoals()).isEqualTo(1);
        assertThat(outcome.awayGoals()).isEqualTo(2);
        assertThat(MatchResult.of(outcome.homeGoals(), outcome.awayGoals())).isEqualTo(MatchResult.VISITANTE);
    }

    @Test
    void defenderAlwaysUsesDefendingAndKeepsTheMomentData() {
        MatchOutcome outcome = MatchEngine.play(aguias(), delta());

        assertThat(outcome.duels()).allSatisfy(duel -> assertThat(duel.defender().attribute()).isEqualTo(DuelAttribute.DEFENDING));
        DuelSide davi = outcome.duels().get(5).defender();
        assertThat(davi.playerId()).isEqualTo(31);
        assertThat(davi.position()).isEqualTo(Position.LAT);
        assertThat(davi.condition()).isEqualTo(Condition.MAL_CONDICIONADO);
    }

    @Test
    void sameStartersAlwaysGiveTheSameResult() {
        assertThat(MatchEngine.play(aguias(), delta())).isEqualTo(MatchEngine.play(aguias(), delta()));
    }

    @Test
    void beingHomeOnlyChangesTheDuelOrder() {
        MatchOutcome reversed = MatchEngine.play(delta(), aguias());

        assertThat(reversed.homeGoals()).isEqualTo(2);
        assertThat(reversed.awayGoals()).isEqualTo(1);
        assertThat(reversed.duels().getFirst().attacker().playerName()).isEqualTo("Ugo Falcão");
    }

    @ParameterizedTest(name = "{0} × 0,85 → {1}")
    @CsvSource({"74, 63", "50, 43", "66, 56", "1, 1", "99, 84", "10, 9", "70, 60"})
    void unfitPlayerLoses15PercentRoundedHalfUp(int attribute, int expected) {
        assertThat(MatchEngine.effectiveValue(attribute, Condition.MAL_CONDICIONADO)).isEqualTo(expected);
        assertThat(MatchEngine.effectiveValue(attribute, Condition.APTO)).isEqualTo(attribute);
    }

    @Test
    void equalGoalsAreADraw() {
        assertThat(MatchResult.of(0, 0)).isEqualTo(MatchResult.EMPATE);
        assertThat(MatchResult.of(3, 3)).isEqualTo(MatchResult.EMPATE);
        assertThat(MatchResult.of(3, 0)).isEqualTo(MatchResult.MANDANTE);
    }

    /**
     * No 1-2-1, os dois MEI seguem o nome: o que vem antes é o 2º jogador de linha.
     */
    @Test
    void sameOutfieldPositionFollowsTheName() {
        List<Player> home = List.of(
                starter(1, 1, "Ana Goleira", Position.GOL, 50, 50, 50, 50),
                starter(2, 1, "Bia Zagueira", Position.ZAG, 50, 50, 50, 50),
                starter(3, 1, "Zeca Meia", Position.MEI, 50, 90, 50, 50),
                starter(4, 1, "Caio Meia", Position.MEI, 50, 60, 50, 50),
                starter(5, 1, "Duda Atacante", Position.ATA, 70, 50, 50, 50));

        MatchOutcome outcome = MatchEngine.play(sheet(1, Formation.ONE_TWO_ONE, home), delta());

        assertThat(outcome.duels().get(1).attacker().playerName()).isEqualTo("Zeca Meia"); // 3º, duelo 2
        assertThat(outcome.duels().get(2).attacker().playerName()).isEqualTo("Caio Meia"); // 2º, duelo 3
        assertThat(outcome.duels().get(5).defender().playerName()).isEqualTo("Caio Meia"); // 2º também defende
        assertThat(outcome.duels().get(4).defender().playerName()).isEqualTo("Bia Zagueira"); // 1º só defende
    }

    @Test
    void lateralsAndDefendersAttackWithDribbling() {
        List<Player> home = List.of(
                starter(1, 1, "Ana Goleira", Position.GOL, 50, 50, 50, 50),
                starter(2, 1, "Bia Zagueira", Position.ZAG, 50, 50, 50, 50),
                starter(3, 1, "Caio Lateral", Position.LAT, 50, 50, 61, 50),
                withCondition(starter(4, 1, "Davi Lateral", Position.LAT, 50, 50, 50, 50), Condition.MAL_CONDICIONADO),
                starter(5, 1, "Duda Atacante", Position.ATA, 70, 50, 50, 50));

        MatchOutcome outcome = MatchEngine.play(sheet(1, Formation.THREE_ONE, home), delta());

        assertThat(outcome.duels().subList(0, 3))
                .extracting(duel -> duel.attacker().position(), duel -> duel.attacker().attribute(), duel -> duel.attacker().value())
                .containsExactly(
                        tuple(Position.ATA, DuelAttribute.SHOOTING, 70),
                        tuple(Position.LAT, DuelAttribute.DRIBBLING, 43),   // Davi, 3º: 50 × 0,85 = 42,5 → 43
                        tuple(Position.LAT, DuelAttribute.DRIBBLING, 61));  // Caio, 2º
    }
}
