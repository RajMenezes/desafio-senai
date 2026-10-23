package br.com.footballmarket.player.application;

import br.com.footballmarket.player.domain.Player;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Filtros da listagem que o banco resolve sobre colunas gravadas. Busca e faixa de valor de mercado
 * dependem de cálculo (texto sem acentos, RN05) e ficam no serviço.
 * <p>
 * Fica na camada de aplicação, ao lado do {@link PlayerQuery} que traduz, para não criar ciclo com a infra.
 */
final class PlayerSpecifications {

    private PlayerSpecifications() {
    }

    static Specification<Player> matching(PlayerQuery query) {
        List<Specification<Player>> filters = new ArrayList<>();
        filters.add(notDeleted());
        if (query.position() != null) {
            filters.add(attributeEquals("position", query.position()));
        }
        if (query.condition() != null) {
            filters.add(attributeEquals("condition", query.condition()));
        }
        if (query.free() != null) {
            filters.add(query.free() ? withoutClub() : withClub());
        }
        if (query.clubId() != null) {
            filters.add(attributeEquals("clubId", query.clubId()));
        }
        if (query.minAge() != null) {
            filters.add((root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("age"), query.minAge()));
        }
        if (query.maxAge() != null) {
            filters.add((root, cq, cb) -> cb.lessThanOrEqualTo(root.get("age"), query.maxAge()));
        }
        return Specification.allOf(filters);
    }

    static Specification<Player> notDeleted() {
        return (root, cq, cb) -> cb.isNull(root.get("deletedAt"));
    }

    private static Specification<Player> withoutClub() {
        return (root, cq, cb) -> cb.isNull(root.get("clubId"));
    }

    private static Specification<Player> withClub() {
        return (root, cq, cb) -> cb.isNotNull(root.get("clubId"));
    }

    private static Specification<Player> attributeEquals(String attribute, Object value) {
        return (root, cq, cb) -> cb.equal(root.get(attribute), value);
    }
}
