package br.com.footballmarket.player.domain;

import java.math.BigDecimal;

// Condição física, com o desconto no valor de mercado (RN02).
public enum Condition {

    APTO(BigDecimal.ZERO),
    MAL_CONDICIONADO(BigDecimal.valueOf(15)),
    DOENTE(BigDecimal.valueOf(30));

    private final BigDecimal discountPercent;

    Condition(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }

    public BigDecimal discountPercent() {
        return discountPercent;
    }


     // De DOENTE não se vai direto para APTO
    public boolean canChangeTo(Condition target) {
        return !(this == DOENTE && target == APTO);
    }
}
