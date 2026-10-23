package br.com.footballmarket.market.infra;

import br.com.footballmarket.market.domain.MarketTransaction;
import br.com.footballmarket.market.domain.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TransactionRepository extends JpaRepository<MarketTransaction, Integer> {

    boolean existsByClubIdAndPlayerIdAndType(Integer clubId, Integer playerId, TransactionType type);

    /**
     * Histórico do clube (RN17): mais recente primeiro; no mesmo instante, id decrescente.
     */
    @Query("""
            select t from MarketTransaction t
            where t.clubId = :clubId and t.type in :types
            order by t.createdAt desc, t.id desc""")
    List<MarketTransaction> findHistory(@Param("clubId") Integer clubId,
                                        @Param("types") Collection<TransactionType> types);

    @Query("""
            select t from MarketTransaction t
            where t.clubId = :clubId and t.type in :types and t.playerId = :playerId
            order by t.createdAt desc, t.id desc""")
    List<MarketTransaction> findHistoryOfPlayer(@Param("clubId") Integer clubId,
                                                @Param("types") Collection<TransactionType> types,
                                                @Param("playerId") Integer playerId);
}
