package br.com.footballmarket.shared.pagination;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class PageResultTest {

    /**
     * Tabela da RN07.
     */
    @ParameterizedTest(name = "{0} itens, limit {1}, page {2}")
    @CsvSource({
            // itens, limit, page, primeiro, quantidade, total_pages
            "23, 10, 1,  1, 10, 3",
            "23, 10, 3, 21,  3, 3",
            "23, 10, 4,  0,  0, 3",
            "23, 50, 1,  1, 23, 1",
            " 0, 10, 1,  0,  0, 0",
            "20, 10, 2, 11, 10, 2"
    })
    void slicesAfterFilteringAndCountsEverything(int items, int limit, int page,
                                                 int first, int count, long totalPages) {
        List<Integer> all = IntStream.rangeClosed(1, items).boxed().toList();

        PageResult<Integer> result = PageResult.slice(all, page, limit);

        assertThat(result.items()).hasSize(count);
        if (count > 0) {
            assertThat(result.items().getFirst()).isEqualTo(first);
        }
        assertThat(result.totalItems()).isEqualTo(items);
        assertThat(result.totalPages()).isEqualTo(totalPages);
        assertThat(result.page()).isEqualTo(page);
        assertThat(result.limit()).isEqualTo(limit);
    }

    @ParameterizedTest
    @CsvSource({"2147483647, 50", "42949673, 50"})
    void hugePageDoesNotOverflow(int page, int limit) {
        assertThat(PageResult.slice(List.of(1, 2, 3), page, limit).items()).isEmpty();
    }
}
