package br.com.footballmarket.market.application;

import br.com.footballmarket.club.application.ClubService;
import br.com.footballmarket.market.domain.MarketTransaction;
import br.com.footballmarket.market.domain.TransactionCsv;
import br.com.footballmarket.market.domain.TransactionSummary;
import br.com.footballmarket.market.infra.TransactionRepository;
import br.com.footballmarket.player.application.PlayerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Histórico de transações do clube (RN17) e a exportação em CSV (RN18), com os mesmos filtros.
 */
@Service
@Transactional(readOnly = true)
public class TransactionHistoryService {

    private final ClubService clubs;
    private final PlayerService players;
    private final TransactionRepository transactions;

    public TransactionHistoryService(ClubService clubs, PlayerService players, TransactionRepository transactions) {
        this.clubs = clubs;
        this.players = players;
        this.transactions = transactions;
    }

    /**
     * Clube inexistente → 404; jogador do filtro inexistente ou excluído → 404 (V8). Jogador que existe
     * mas não tem transações no clube → lista vazia e totais zerados.
     */
    public TransactionHistory history(Integer clubId, TransactionFilter filter) {
        List<MarketTransaction> found = find(clubId, filter);
        return new TransactionHistory(found, TransactionSummary.of(found));
    }

    public String exportCsv(Integer clubId, TransactionFilter filter) {
        return TransactionCsv.write(find(clubId, filter));
    }

    private List<MarketTransaction> find(Integer clubId, TransactionFilter filter) {
        clubs.requireExists(clubId);
        if (filter.playerId() == null) {
            return transactions.findHistory(clubId, filter.types());
        }
        players.findById(filter.playerId());
        return transactions.findHistoryOfPlayer(clubId, filter.types(), filter.playerId());
    }
}
