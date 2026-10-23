package br.com.footballmarket.market.application;

import br.com.footballmarket.market.domain.Contract;
import br.com.footballmarket.market.domain.MarketTransaction;
import br.com.footballmarket.shared.money.Money;

/**
 * O que a compra gravou: a transação COMPRA, o contrato ATIVO e o caixa do clube depois da compra.
 */
public record PurchaseResult(MarketTransaction transaction, Contract contract, Money clubCash) {
}
