package br.com.footballmarket.player.application;

/**
 * O que o módulo de jogadores precisa saber sobre clubes. Implementada pelo módulo de clubes,
 * para que a dependência entre os dois vá num sentido só (clube → jogador).
 */
public interface ClubDirectory {

    boolean exists(Integer clubId);
}
