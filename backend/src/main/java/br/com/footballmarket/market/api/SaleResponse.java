package br.com.footballmarket.market.api;

import br.com.footballmarket.market.application.SaleResult;
import br.com.footballmarket.market.domain.ContractStatus;
import br.com.footballmarket.market.domain.MarketTransaction;
import br.com.footballmarket.market.domain.TransactionType;
import br.com.footballmarket.shared.money.Money;

import java.time.OffsetDateTime;

/**
 * Resposta da venda (2.3.2): a transação VENDA, o valor de compra, a fidelidade no momento da venda,
 * a situação final do contrato e o novo caixa do clube.
 */
public record SaleResponse(
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
        OffsetDateTime createdAt,
        Money purchaseValue,
        long remainingDays,
        int penaltyRate,
        ContractStatus contractStatus,
        Money clubCash) {

    public static SaleResponse from(SaleResult result) {
        MarketTransaction transaction = result.transaction();
        return new SaleResponse(
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
                transaction.getCreatedAt(),
                result.contract().getPurchaseValue(),
                result.remainingDays(),
                result.penaltyRate(),
                result.contract().getStatus(),
                result.clubCash());
    }
}
