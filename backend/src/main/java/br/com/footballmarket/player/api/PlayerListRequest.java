package br.com.footballmarket.player.api;

import br.com.footballmarket.player.application.PlayerQuery;
import br.com.footballmarket.player.domain.Condition;
import br.com.footballmarket.player.domain.PlayerSort;
import br.com.footballmarket.player.domain.Position;
import br.com.footballmarket.shared.error.BadRequestException;
import br.com.footballmarket.shared.money.Money;
import br.com.footballmarket.shared.text.TextNormalizer;
import br.com.footballmarket.shared.text.TextRules;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Query params do {@code GET /players} como chegaram (formato já conferido pelo {@code StrictParamBinding}).
 * {@link #toQuery()} confere limites, combinações e a ordenação, e aplica os padrões (tabela "Filtros e query params").
 */
record PlayerListRequest(
        String search,
        Position position,
        Condition condition,
        Boolean free,
        Integer clubId,
        Integer minAge,
        Integer maxAge,
        BigDecimal minValue,
        BigDecimal maxValue,
        String sort,
        Integer page,
        Integer limit) {

    static final int MIN_SEARCH_LENGTH = 2;
    static final int MAX_SEARCH_LENGTH = 100;
    static final int MIN_AGE = 16;
    static final int MAX_AGE = 45;
    static final BigDecimal MAX_VALUE = new BigDecimal("550");
    static final int DEFAULT_PAGE = 1;
    static final int DEFAULT_LIMIT = 10;
    static final int MAX_LIMIT = 50;
    static final int MAX_SORT_CRITERIA = 2;

    PlayerQuery toQuery() {
        requireInRange("min_age", minAge, MIN_AGE, MAX_AGE);
        requireInRange("max_age", maxAge, MIN_AGE, MAX_AGE);
        if (minAge != null && maxAge != null && minAge > maxAge) {
            throw new BadRequestException("'min_age' não pode ser maior que 'max_age'.");
        }
        requireValueAtMostMax("min_value", minValue);
        requireValueAtMostMax("max_value", maxValue);
        if (minValue != null && maxValue != null && minValue.compareTo(maxValue) > 0) {
            throw new BadRequestException("'min_value' não pode ser maior que 'max_value'.");
        }
        if (Boolean.TRUE.equals(free) && clubId != null) {
            throw new BadRequestException("'free=true' não pode ser combinado com 'club_id'.");
        }
        if (limit != null && limit > MAX_LIMIT) {
            throw new BadRequestException("O parâmetro 'limit' deve estar entre 1 e " + MAX_LIMIT + ".");
        }
        return new PlayerQuery(
                normalizedSearch(),
                position,
                condition,
                free,
                clubId,
                minAge,
                maxAge,
                minValue == null ? null : Money.of(minValue),
                maxValue == null ? null : Money.of(maxValue),
                parseSort(),
                page == null ? DEFAULT_PAGE : page,
                limit == null ? DEFAULT_LIMIT : limit);
    }

    /**
     * V4: espaços das pontas removidos e repetidos colapsados antes de medir; texto que fica vazio é recusado.
     */
    private String normalizedSearch() {
        if (search == null) {
            return null;
        }
        String normalized = TextNormalizer.normalize(search);
        if (normalized == null) {
            throw new BadRequestException("O parâmetro 'search' não pode ser vazio.");
        }
        int length = TextRules.length(normalized);
        if (length < MIN_SEARCH_LENGTH || length > MAX_SEARCH_LENGTH) {
            throw new BadRequestException("O parâmetro 'search' deve ter entre " + MIN_SEARCH_LENGTH
                    + " e " + MAX_SEARCH_LENGTH + " caracteres.");
        }
        return normalized;
    }

    /**
     * Até dois critérios separados por vírgula, sem espaços e sem repetir o campo
     * ({@code value_asc,value_desc} repete "value").
     */
    private List<PlayerSort> parseSort() {
        if (sort == null) {
            return PlayerSort.DEFAULT;
        }
        String[] tokens = sort.split(",", -1);
        if (tokens.length > MAX_SORT_CRITERIA) {
            throw new BadRequestException("O parâmetro 'sort' aceita no máximo " + MAX_SORT_CRITERIA + " critérios.");
        }
        List<PlayerSort> criteria = new ArrayList<>();
        for (String token : tokens) {
            PlayerSort criterion = PlayerSort.fromParam(token)
                    .orElseThrow(() -> new BadRequestException("Valor inválido para o parâmetro 'sort': '" + token
                            + "'. Use name, value_asc, value_desc, age_asc, age_desc ou position."));
            if (criteria.stream().anyMatch(chosen -> chosen.field().equals(criterion.field()))) {
                throw new BadRequestException("O parâmetro 'sort' não pode repetir o campo '" + criterion.field() + "'.");
            }
            criteria.add(criterion);
        }
        return criteria;
    }

    private static void requireInRange(String param, Integer value, int min, int max) {
        if (value != null && (value < min || value > max)) {
            throw new BadRequestException("O parâmetro '" + param + "' deve estar entre " + min + " e " + max + ".");
        }
    }

    /**
     * O formato (sem sinal, até 2 casas) já garante o mínimo 0.
     */
    private static void requireValueAtMostMax(String param, BigDecimal value) {
        if (value != null && value.compareTo(MAX_VALUE) > 0) {
            throw new BadRequestException("O parâmetro '" + param + "' deve estar entre 0 e " + MAX_VALUE + ".");
        }
    }
}
