package br.com.footballmarket.club.domain;

import br.com.footballmarket.player.domain.Condition;
import br.com.footballmarket.player.domain.Position;
import br.com.footballmarket.shared.money.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static br.com.footballmarket.club.domain.ClubFixtures.club;
import static br.com.footballmarket.club.domain.ClubFixtures.withCash;
import static br.com.footballmarket.player.domain.PlayerFixtures.meiaLua;
import static br.com.footballmarket.player.domain.PlayerFixtures.player;
import static br.com.footballmarket.player.domain.PlayerFixtures.withCondition;
import static org.assertj.core.api.Assertions.assertThat;

class ValuedClubTest {

    @Test
    void emptySquadIsWorthTheCash() {
        ValuedClub valued = ValuedClub.of(ClubFixtures.aguias(), List.of());

        assertThat(valued.squadValue()).isEqualTo(Money.ZERO);
        assertThat(valued.netWorth()).isEqualTo(Money.of("300.00"));
    }

    @Test
    void squadValueSumsMarketValuesAndNetWorthAddsTheCash() {
        // exemplo 2.2.2: caixa 261,15 e Meia Lua MAL_CONDICIONADO valendo 37,00
        ValuedClub single = ValuedClub.of(withCash(ClubFixtures.aguias(), "261.15"), List.of(
                withCondition(meiaLua(), Condition.MAL_CONDICIONADO)));
        assertThat(single.squadValue()).isEqualTo(Money.of("37.00"));
        assertThat(single.netWorth()).isEqualTo(Money.of("298.15"));

        ValuedClub two = ValuedClub.of(ClubFixtures.aguias(), List.of(
                player("Ana Lima", Position.MEI, 25),                                          // 40,00
                withCondition(player("Bia Reis", Position.MEI, 25), Condition.MAL_CONDICIONADO))); // 34,00
        assertThat(two.squadValue()).isEqualTo(Money.of("74.00"));
        assertThat(two.netWorth()).isEqualTo(Money.of("374.00"));
    }

    private static List<String> sortedNames(ClubSort sort, ValuedClub... clubs) {
        return Stream.of(clubs).sorted(sort.ordering()).map(valued -> valued.club().getName()).toList();
    }

    @Test
    void nameOrderUsesTheAlphabeticalRuleAndBreaksTiesById() {
        ValuedClub zebras = ValuedClub.of(club("Zebras FC", "ZFC", 1), List.of());
        ValuedClub plain = ValuedClub.of(club("Aguias FC", "AFC", 4), List.of());
        ValuedClub accented = ValuedClub.of(club("Águias FC", "AGU", 2), List.of());
        ValuedClub epsilon = ValuedClub.of(club("Épsilon EC", "EEC", 3), List.of());

        assertThat(sortedNames(ClubSort.NAME, zebras, plain, accented, epsilon))
                .containsExactly("Águias FC", "Aguias FC", "Épsilon EC", "Zebras FC");
    }

    @Test
    void netWorthOrderUsesCashPlusSquadAndTiesFallBackToName() {
        ValuedClub rich = ValuedClub.of(club("Bravos FC", "BFC", 1), List.of(player("Ana Lima", Position.MEI, 25)));
        ValuedClub poorB = ValuedClub.of(withCash(club("Corsários EC", "CEC", 2), "100.00"), List.of());
        ValuedClub poorA = ValuedClub.of(withCash(club("Alvos FC", "AFC", 3), "100.00"), List.of());

        assertThat(sortedNames(ClubSort.NET_WORTH_DESC, poorB, rich, poorA))
                .containsExactly("Bravos FC", "Alvos FC", "Corsários EC");
        assertThat(sortedNames(ClubSort.NET_WORTH_ASC, rich, poorB, poorA))
                .containsExactly("Alvos FC", "Corsários EC", "Bravos FC");
    }

    @ParameterizedTest
    @ValueSource(strings = {"NAME", "net_worth", "name,net_worth_asc", " name", ""})
    void sortAcceptsOnlyOneExactValue(String param) {
        assertThat(ClubSort.fromParam(param)).isEmpty();
    }
}
