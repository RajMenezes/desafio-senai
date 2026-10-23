package br.com.footballmarket.player.domain;

import java.util.stream.IntStream;

/**
 * Os seis indicadores técnicos do jogador, informados no cadastro e imutáveis depois (RN06).
 */
public record Attributes(int pace, int shooting, int passing, int dribbling, int defending, int physical) {

    public static final int MAX_SUM = 480;

    public int sum() {
        return values().sum();
    }

    public int highest() {
        return values().max().orElseThrow();
    }

    private IntStream values() {
        return IntStream.of(pace, shooting, passing, dribbling, defending, physical);
    }
}

