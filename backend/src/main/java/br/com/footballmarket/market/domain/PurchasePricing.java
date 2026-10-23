package br.com.footballmarket.market.domain;

import br.com.footballmarket.shared.error.BusinessRuleException;
import br.com.footballmarket.shared.money.Money;

import java.math.BigDecimal;

/**
 * RN13: taxa de compra e limite de 50% do caixa.
 */
public final class PurchasePricing {

    static final Money FEE_THRESHOLD = Money.of("30.00");
    static final BigDecimal FEE_RATE = new BigDecimal("0.05");

    private PurchasePricing() {
    }

    /**
     * 5% do valor de mercado, arredondada, só quando o valor é maior que 30,00.
     */
    public static Money fee(Money marketValue) {
        if (!marketValue.isGreaterThan(FEE_THRESHOLD)) {
            return Money.ZERO;
        }
        return Money.of(marketValue.amount().multiply(FEE_RATE));
    }

    /**
     * O custo pode ser igual à metade do caixa, nunca maior. A metade não é arredondada
     * (caixa 261,15 → limite 130,575), por isso a comparação é custo × 2 contra o caixa.
     */
    public static void requireAffordable(Money totalCost, Money cash) {
        BigDecimal doubledCost = totalCost.amount().multiply(BigDecimal.TWO);
        if (doubledCost.compareTo(cash.amount()) > 0) {
            // divisão exata: 100,00 → 50,00; 261,15 → 130,575
            String limit = cash.amount().divide(BigDecimal.TWO).toPlainString().replace('.', ',');
            throw new BusinessRuleException("O custo da compra (" + totalCost.toBrazilianFormat()
                    + ") passa do limite de 50% do caixa (" + limit + ").");
        }
    }
}
