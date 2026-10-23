package br.com.footballmarket.club.domain;

import br.com.footballmarket.shared.money.Money;
import org.springframework.test.util.ReflectionTestUtils;

import static br.com.footballmarket.player.domain.PlayerFixtures.NOW;

/**
 * Clubes prontos para os testes. O "Águias FC" é o clube dos exemplos do enunciado.
 */
public final class ClubFixtures {

    private ClubFixtures() {
    }

    public static Club aguias() {
        return Club.create("Águias FC", "AGU", NOW);
    }

    public static Club club(String name, String acronym, int id) {
        return withId(Club.create(name, acronym, NOW), id);
    }

    public static Club withId(Club club, int id) {
        ReflectionTestUtils.setField(club, "id", id);
        return club;
    }

    /**
     * Enquanto compra e venda (Etapas 5 e 6) não existem, o caixa é ajustado direto.
     */
    public static Club withCash(Club club, String cash) {
        ReflectionTestUtils.setField(club, "cash", Money.of(cash));
        return club;
    }
}
