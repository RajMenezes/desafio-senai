package br.com.footballmarket.market.api;

import br.com.footballmarket.market.application.TransactionHistory;
import br.com.footballmarket.market.domain.TransactionSummary;

import java.util.List;

/**
 * {@code {"data": [...], "summary": {...}}} (2.3.3). O {@code summary} respeita os filtros.
 */
public record TransactionHistoryResponse(List<TransactionResponse> data, TransactionSummary summary) {

    public static TransactionHistoryResponse from(TransactionHistory history) {
        return new TransactionHistoryResponse(
                history.transactions().stream().map(TransactionResponse::from).toList(),
                history.summary());
    }
}
