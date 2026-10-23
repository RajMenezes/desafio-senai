package br.com.footballmarket.club.domain;

import br.com.footballmarket.shared.text.TextRules;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

/**
 * Ordenação de {@code GET /clubs} (RN11): pelo nome ou pelo patrimônio calculado.
 * Em qualquer uma, o empate é desfeito pelo nome e depois pelo id crescente.
 */
public enum ClubSort {

    NAME("name"),
    NET_WORTH_ASC("net_worth_asc"),
    NET_WORTH_DESC("net_worth_desc");

    public static final ClubSort DEFAULT = NAME;

    private static final Comparator<ValuedClub> BY_NAME_THEN_ID = Comparator
            .comparing((ValuedClub valued) -> valued.club().getName(), TextRules.ALPHABETICAL)
            .thenComparing(valued -> valued.club().getId());

    private final String param;

    ClubSort(String param) {
        this.param = param;
    }

    /**
     * Valor exato do query param, sensível a maiúsculas; só um critério.
     */
    public static Optional<ClubSort> fromParam(String param) {
        return Arrays.stream(values()).filter(sort -> sort.param.equals(param)).findFirst();
    }

    public Comparator<ValuedClub> ordering() {
        Comparator<ValuedClub> byNetWorth = Comparator.comparing(ValuedClub::netWorth);
        return switch (this) {
            case NAME -> BY_NAME_THEN_ID;
            case NET_WORTH_ASC -> byNetWorth.thenComparing(BY_NAME_THEN_ID);
            case NET_WORTH_DESC -> byNetWorth.reversed().thenComparing(BY_NAME_THEN_ID);
        };
    }
}
