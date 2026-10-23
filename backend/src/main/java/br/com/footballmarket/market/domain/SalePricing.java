package br.com.footballmarket.market.domain;

import br.com.footballmarket.shared.money.Money;

import java.math.BigDecimal;

/**
 * RN15 e RN16: multa por fidelidade e comissão progressiva sobre o lucro da venda.
 */
public final class SalePricing {

    static final Money PENALTY_CAP = Money.of("10.00");
    static final BigDecimal COMMISSION_BRACKET = new BigDecimal("10.00");
    static final BigDecimal LOWER_RATE = new BigDecimal("0.10");
    static final BigDecimal UPPER_RATE = new BigDecimal("0.20");
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private SalePricing() {
    }

    /**
     * Valor de mercado × percentual, arredondado e limitado a 10,00.
     */
    public static Money penalty(Money marketValue, int penaltyRate) {
        Money penalty = Money.of(marketValue.amount().multiply(BigDecimal.valueOf(penaltyRate)).divide(HUNDRED));
        return penalty.min(PENALTY_CAP);
    }

    /**
     * Só com lucro maior que zero: 10% sobre a parte até 10,00 mais 20% sobre a parte acima,
     * somados e arredondados uma única vez (10,01 → 1,002 → 1,00).
     */
    public static Money commission(Money profit) {
        if (!profit.isPositive()) {
            return Money.ZERO;
        }
        BigDecimal lower = profit.amount().min(COMMISSION_BRACKET);
        BigDecimal upper = profit.amount().subtract(lower);
        return Money.of(lower.multiply(LOWER_RATE).add(upper.multiply(UPPER_RATE)));
    }
}
