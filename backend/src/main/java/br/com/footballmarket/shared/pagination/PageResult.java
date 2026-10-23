package br.com.footballmarket.shared.pagination;

import java.util.List;
import java.util.function.Function;

/**
 * Uma página de resultados com o total de itens que atendem à consulta (RN07).
 * Página além da última é válida e vem vazia.
 */
public record PageResult<T>(List<T> items, int page, int limit, long totalItems) {

    public PageResult {
        items = List.copyOf(items);
    }

    /**
     * Recorta a página de uma lista já filtrada e ordenada: a paginação vem sempre depois.
     */
    public static <T> PageResult<T> slice(List<T> all, int page, int limit) {
        long from = (long) (page - 1) * limit;
        if (from >= all.size()) {
            return new PageResult<>(List.of(), page, limit, all.size());
        }
        int to = (int) Math.min(from + limit, all.size());
        return new PageResult<>(all.subList((int) from, to), page, limit, all.size());
    }

    /**
     * {@code total_items ÷ limit}, arredondado para cima; 0 quando não há itens.
     */
    public long totalPages() {
        return (totalItems + limit - 1) / limit;
    }

    public <R> PageResult<R> map(Function<? super T, ? extends R> mapper) {
        return new PageResult<>(items.stream().<R>map(mapper).toList(), page, limit, totalItems);
    }
}
