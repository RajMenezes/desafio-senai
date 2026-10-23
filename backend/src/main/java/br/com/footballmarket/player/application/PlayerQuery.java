package br.com.footballmarket.player.application;

import br.com.footballmarket.player.domain.Condition;
import br.com.footballmarket.player.domain.PlayerSort;
import br.com.footballmarket.player.domain.Position;
import br.com.footballmarket.shared.money.Money;

import java.util.List;

/**
 * Busca, filtros, ordenação e página da listagem (RN07), já validados.
 * Filtro {@code null} não filtra; numa faixa, limite {@code null} é o extremo da faixa.
 */
public record PlayerQuery(
        String search,
        Position position,
        Condition condition,
        Boolean free,
        Integer clubId,
        Integer minAge,
        Integer maxAge,
        Money minValue,
        Money maxValue,
        List<PlayerSort> sort,
        int page,
        int limit) {

    public PlayerQuery {
        sort = List.copyOf(sort);
    }
}
