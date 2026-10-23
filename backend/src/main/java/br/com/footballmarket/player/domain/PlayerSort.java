package br.com.footballmarket.player.domain;

import br.com.footballmarket.shared.text.TextRules;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Critérios de ordenação da listagem (RN07), cada um com o seu comparador (Strategy).
 * O valor é sempre o de mercado; a posição segue a ordem GOL, ZAG, LAT, MEI, ATA (a de declaração do enum).
 */
public enum PlayerSort {

    NAME("name", "name", Names.ORDER),
    VALUE_ASC("value_asc", "value", Comparator.comparing(Player::marketValue)),
    VALUE_DESC("value_desc", "value", Comparator.comparing(Player::marketValue).reversed()),
    AGE_ASC("age_asc", "age", Comparator.comparingInt(Player::getAge)),
    AGE_DESC("age_desc", "age", Comparator.comparingInt(Player::getAge).reversed()),
    POSITION("position", "position", Comparator.comparing(Player::getPosition));

    public static final List<PlayerSort> DEFAULT = List.of(NAME);

    private final String param;
    private final String field;
    private final Comparator<Player> comparator;

    PlayerSort(String param, String field, Comparator<Player> comparator) {
        this.param = param;
        this.field = field;
        this.comparator = comparator;
    }

    /**
     * Valor exato do query param ({@code value_asc}...), sensível a maiúsculas.
     */
    public static Optional<PlayerSort> fromParam(String param) {
        return Arrays.stream(values()).filter(sort -> sort.param.equals(param)).findFirst();
    }

    /**
     * Campo ordenado: {@code value_asc} e {@code value_desc} ordenam o mesmo campo e não podem vir juntos.
     */
    public String field() {
        return field;
    }

    /**
     * Os critérios na ordem pedida e, no fim, o desempate pelo nome e pelo id crescente.
     */
    public static Comparator<Player> ordering(List<PlayerSort> criteria) {
        Comparator<Player> ordering = criteria.stream()
                .map(sort -> sort.comparator)
                .reduce(Comparator::thenComparing)
                .orElse(Names.ORDER);
        return ordering.thenComparing(Names.ORDER).thenComparing(Player::getId);
    }

    /**
     * Ordem do elenco (RN11): posição, nome e id.
     */
    public static Comparator<Player> squadOrdering() {
        return ordering(List.of(POSITION));
    }

    /**
     * Ordem alfabética das Premissas ({@link TextRules#ALPHABETICAL}).
     */
    private static final class Names {
        static final Comparator<Player> ORDER = Comparator.comparing(Player::getName, TextRules.ALPHABETICAL);
    }
}
