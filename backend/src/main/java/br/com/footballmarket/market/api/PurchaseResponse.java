package br.com.footballmarket.market.api;

import br.com.footballmarket.market.application.PurchaseResult;
import br.com.footballmarket.market.domain.Contract;
import br.com.footballmarket.market.domain.ContractStatus;
import br.com.footballmarket.market.domain.MarketTransaction;
import br.com.footballmarket.market.domain.TransactionType;
import br.com.footballmarket.shared.money.Money;

import java.time.OffsetDateTime;

/**
 * Resposta da compra (2.3.1): a transação COMPRA, o contrato criado e o novo caixa do clube.
 */
public record PurchaseResponse(
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
        ContractResponse contract,
        Money clubCash) {

    public record ContractResponse(Money purchaseValue, OffsetDateTime loyaltyUntil, ContractStatus status) {
    }

    public static PurchaseResponse from(PurchaseResult result) {
        MarketTransaction transaction = result.transaction();
        Contract contract = result.contract();
        return new PurchaseResponse(
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
                new ContractResponse(contract.getPurchaseValue(), contract.getLoyaltyUntil(), contract.getStatus()),
                result.clubCash());
    }
}
