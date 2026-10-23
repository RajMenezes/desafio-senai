package br.com.footballmarket.market.api;

import br.com.footballmarket.market.domain.MarketTransaction;
import br.com.footballmarket.market.domain.TransactionType;
import br.com.footballmarket.shared.money.Money;

import java.time.OffsetDateTime;

/**
 * Transação como aparece no histórico (2.3.3).
 */
public record TransactionResponse(
        Integer id,
        Integer clubId,
        Integer playerId,
        String playerName,
        TransactionType type,
        Money value,
        Money fee,
        Money penalty,
        Money profit,
        Money cashChange,
        OffsetDateTime createdAt) {

    public static TransactionResponse from(MarketTransaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getClubId(),
                transaction.getPlayerId(),
                transaction.getPlayerName(),
                transaction.getType(),
                transaction.getValue(),
                transaction.getFee(),
                transaction.getPenalty(),
                transaction.getProfit(),
                transaction.getCashChange(),
                transaction.getCreatedAt());
    }
}
