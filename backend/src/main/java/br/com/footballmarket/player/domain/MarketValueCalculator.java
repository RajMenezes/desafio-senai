package br.com.footballmarket.player.domain;

import br.com.footballmarket.shared.money.Money;

import java.math.BigDecimal;

/**
 * RN02 a RN05: valor de mercado, calculado a cada consulta, nesta ordem:
 *     desconto por condição (RN02), ajustado pela agilidade só fora de APTO (RN03);
 *     percentual total = ajuste por idade (RN04) − desconto por condição;
 *     teto do desconto: o percentual total nunca fica abaixo de −40%;
 *     valor de mercado = valor base × (100 + percentual total) ÷ 100, arredondado;
 *     piso de 1,00.</li>
 */
public final class MarketValueCalculator {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal TWO = BigDecimal.valueOf(2);
    private static final BigDecimal LOW_AGILITY_PENALTY = BigDecimal.TEN;
    private static final BigDecimal AGE_ADJUSTMENT = BigDecimal.TEN;
    private static final BigDecimal MAX_TOTAL_DISCOUNT = BigDecimal.valueOf(-40);
    private static final Money FLOOR = Money.of("1.00");

    private MarketValueCalculator() {
    }

    public static Money calculate(Money baseValue, Condition condition, int agility, int age) {
        BigDecimal totalPercent = ageAdjustmentPercent(age)
                .subtract(conditionDiscountPercent(condition, agility))
                .max(MAX_TOTAL_DISCOUNT);
        BigDecimal value = baseValue.amount()
                .multiply(HUNDRED.add(totalPercent))
                .divide(HUNDRED);
        return Money.of(value).max(FLOOR);
    }

    /**
     * RN02 + RN03: agilidade de 80 a 100 corta o desconto pela metade; de 1 a 30 soma 10 pontos;
     * de 31 a 79 não altera. Em APTO a agilidade não tem efeito.
     */
    static BigDecimal conditionDiscountPercent(Condition condition, int agility) {
        BigDecimal discount = condition.discountPercent();
        if (condition == Condition.APTO) {
            return discount;
        }
        if (agility >= 80) {
            return discount.divide(TWO);
        }
        if (agility <= 30) {
            return discount.add(LOW_AGILITY_PENALTY);
        }
        return discount;
    }

    /**
     * RN04: até 21 anos +10%; de 22 a 31, 0%; 32 ou mais, −10%. Vale para qualquer condição.
     */
    static BigDecimal ageAdjustmentPercent(int age) {
        if (age <= 21) {
            return AGE_ADJUSTMENT;
        }
        if (age >= 32) {
            return AGE_ADJUSTMENT.negate();
        }
        return BigDecimal.ZERO;
    }
}
