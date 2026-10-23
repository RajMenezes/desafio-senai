package br.com.footballmarket.market.domain;

import br.com.footballmarket.shared.money.Money;

import java.util.List;

/**
 * Totais das transações filtradas (RN17). Cada parcela já tem 2 casas, então as somas são exatas.
 *
 * @param totalSpent     {@code value + fee} das compras
 * @param totalReceived  {@code cash_change} das vendas
 * @param totalFees      {@code fee} de todas (taxas e comissões)
 * @param totalPenalties {@code penalty} das vendas
 * @param totalProfit    {@code profit} das vendas, com sinal
 */
public record TransactionSummary(Money totalSpent, Money totalReceived, Money totalFees,
                                 Money totalPenalties, Money totalProfit) {

    public static TransactionSummary of(List<MarketTransaction> transactions) {
        Money spent = Money.ZERO;
        Money received = Money.ZERO;
        Money fees = Money.ZERO;
        Money penalties = Money.ZERO;
        Money profit = Money.ZERO;
        for (MarketTransaction transaction : transactions) {
            fees = fees.plus(transaction.getFee());
            if (transaction.getType() == TransactionType.COMPRA) {
                spent = spent.plus(transaction.getValue()).plus(transaction.getFee());
            } else {
                received = received.plus(transaction.getCashChange());
                penalties = penalties.plus(transaction.getPenalty());
                profit = profit.plus(transaction.getProfit());
            }
        }
        return new TransactionSummary(spent, received, fees, penalties, profit);
    }
}
