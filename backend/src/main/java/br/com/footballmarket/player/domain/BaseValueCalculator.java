package br.com.footballmarket.player.domain;

import br.com.footballmarket.shared.money.Money;

import java.math.BigDecimal;

/**
 * RN06: valor base, calculado uma única vez no cadastro.
 * <ol>
 *     <li>Parte do overall, progressiva: 0,50 por ponto de 41 a 60, 2,00 por ponto de 61 a 75 e
 *     6,00 por ponto de 76 a 99; até 40 não soma nada.</li>
 *     <li>Jovem promessa (até 21 anos no cadastro): parte do overall × (100 + potencial − overall) ÷ 100.</li>
 *     <li>Arredondamento único para 2 casas, com piso de 1,00.</li>
 * </ol>
 */
public final class BaseValueCalculator {

    private static final int YOUNG_PROSPECT_MAX_AGE = 21;
    private static final Money FLOOR = Money.of("1.00");

    private static final Band[] BANDS = {
            new Band(40, 60, new BigDecimal("0.50")),
            new Band(60, 75, new BigDecimal("2.00")),
            new Band(75, 99, new BigDecimal("6.00"))
    };

    private BaseValueCalculator() {
    }

    public static Money calculate(int overall, int potential, int age) {
        BigDecimal overallPart = BigDecimal.ZERO;
        for (Band band : BANDS) {
            overallPart = overallPart.add(band.valueFor(overall));
        }
        if (age <= YOUNG_PROSPECT_MAX_AGE) {
            BigDecimal multiplierPercent = BigDecimal.valueOf(100L + potential - overall);
            overallPart = overallPart.multiply(multiplierPercent).movePointLeft(2);
        }
        return Money.of(overallPart).max(FLOOR);
    }

    /**
     * Faixa progressiva: cada ponto de overall acima de {@code after} e até {@code upTo} vale {@code perPoint}.
     */
    private record Band(int after, int upTo, BigDecimal perPoint) {

        BigDecimal valueFor(int overall) {
            int points = Math.max(0, Math.min(overall, upTo) - after);
            return perPoint.multiply(BigDecimal.valueOf(points));
        }
    }
}
