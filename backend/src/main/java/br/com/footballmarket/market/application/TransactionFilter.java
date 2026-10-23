package br.com.footballmarket.market.application;

import br.com.footballmarket.market.domain.TransactionType;

import java.util.EnumSet;
import java.util.Set;

/**
 * Filtros do histórico, já validados (RN17, RN18).
 *
 * @param types    tipos aceitos; todos quando o filtro não vem
 * @param playerId {@code null} não filtra
 */
public record TransactionFilter(Set<TransactionType> types, Integer playerId) {

    public static final Set<TransactionType> ALL_TYPES = EnumSet.allOf(TransactionType.class);

    public TransactionFilter {
        types = Set.copyOf(types);
    }
}
