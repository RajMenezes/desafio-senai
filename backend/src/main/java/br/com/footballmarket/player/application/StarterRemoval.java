package br.com.footballmarket.player.application;

/**
 * Tira o jogador da escalação do clube quando ele passa a DOENTE (RN19, item 5), na mesma operação.
 * Implementada pelo módulo de clubes, para que a dependência vá num sentido só (clube → jogador).
 */
public interface StarterRemoval {

    void removeStarter(Integer playerId);
}
