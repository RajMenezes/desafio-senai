package br.com.footballmarket.market.application;

import br.com.footballmarket.market.domain.Contract;
import br.com.footballmarket.market.domain.MarketTransaction;
import br.com.footballmarket.shared.money.Money;

/**
 * O que a venda gravou e calculou: a transação VENDA, o contrato encerrado ou rescindido,
 * os dias restantes e o percentual de multa no momento da venda e o novo caixa do clube.
 */
public record SaleResult(MarketTransaction transaction, Contract contract, long remainingDays, int penaltyRate,
                         Money clubCash) {
}
