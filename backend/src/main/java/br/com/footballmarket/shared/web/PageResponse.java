package br.com.footballmarket.shared.web;

import br.com.footballmarket.shared.pagination.PageResult;

import java.util.List;

/**
 * Formato das listagens paginadas: {@code {"data": [...], "meta": {"page", "limit", "total_items", "total_pages"}}}.
 */
public record PageResponse<T>(List<T> data, Meta meta) {

    public record Meta(int page, int limit, long totalItems, long totalPages) {
    }

    public static <T> PageResponse<T> from(PageResult<T> result) {
        return new PageResponse<>(result.items(),
                new Meta(result.page(), result.limit(), result.totalItems(), result.totalPages()));
    }
}
