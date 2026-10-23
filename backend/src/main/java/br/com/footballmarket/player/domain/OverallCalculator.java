package br.com.footballmarket.player.domain;

/**
 * RN06: overall = soma(atributo × peso da posição) ÷ 100, arredondado para o inteiro mais próximo
 * com meio para cima (72,5 → 73; 72,49 → 72).
 */
public final class OverallCalculator {

    private OverallCalculator() {
    }

    public static int calculate(Position position, Attributes attributes) {
        int weightedSum = position.weightedSum(attributes);
        // a soma é sempre positiva, então somar 50 antes da divisão inteira arredonda o meio para cima
        return (weightedSum + 50) / 100;
    }
}
