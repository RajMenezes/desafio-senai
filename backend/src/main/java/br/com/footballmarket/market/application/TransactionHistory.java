package br.com.footballmarket.market.application;

import br.com.footballmarket.market.domain.MarketTransaction;
import br.com.footballmarket.market.domain.TransactionSummary;

import java.util.List;

/**
 * Transações filtradas, na ordem do histórico, e os totais delas.
 */
public record TransactionHistory(List<MarketTransaction> transactions, TransactionSummary summary) {

    public TransactionHistory {
        transactions = List.copyOf(transactions);
    }
}
